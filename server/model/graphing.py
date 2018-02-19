import sys
import os
import re

# This section is necessary for viewing plots.
import holoviews as hv
hv.extension('bokeh','matplotlib')
from sqlalchemy import text

import server.model.connection
import server.model.event as event
import server.model.dal as sm_dal
import server.config as config

engine = server.model.connection.engine


def get_data(tasks, phase='teleop', teams=None):
	teams_tasks_data = list()
	conn = engine.connect()
	evt = event.EventDal.get_current_event()
	event_id = sm_dal.event_ids[evt]
	phase_id = sm_dal.phase_ids[phase]

	if(teams is None):
		teams = Graphing.get_teams()

	for team in teams:
		team_id = sm_dal.team_ids[team]
		team_data = list()

		for task in tasks:
			task_id = sm_dal.task_ids[task]

			sql = text("SELECT * FROM measures WHERE "
					   "event_id = :event_id "
					   "AND task_id = :task_id "
					   "AND team_id = :team_id "
					   "AND phase_id = :phase_id;")

			results = conn.execute(sql, event_id=event_id, task_id=task_id,
								   team_id=team_id, phase_id=phase_id).fetchall()

			for row in results:
				match = sm_dal.match_names[row['match_id']]
				attempts = row['attempts']
				successes = row['successes']
				capability = row['capability']

				team_data.append([team, task, match, successes, attempts])

		teams_tasks_data.append(team_data)

	conn.close()
	return teams_tasks_data


def get_teams():
	all_teams = list()
	sql = text("SELECT DISTINCT team FROM schedules WHERE "
			   "event = :event ORDER BY team;")

	conn = engine.connect()
	results = conn.execute(sql, event=event.EventDal.get_current_event())
	conn.close()

	for row in results:
		all_teams.append(str(row).split('\'')[1])

	return all_teams


def average_tasks(data):
	fixed_data = list()
	for team_data in data:
		task = None
		current_data = list()
		team_fixed = list()
		sum = 1

		for row in team_data:
			if row[1] == task:
				current_data[2] += row[3]
				current_data[3] += row[4]
				sum += 1
			else:
				if task is not None:
					team_fixed.append([row[0], current_data[1], 'na', current_data[2] / sum, current_data[3] / sum])

				current_data = [row[0], row[1], row[3], row[4]]
				task = row[1]
				sum = 1

		team_fixed.append([current_data[0], current_data[1], 'na', current_data[2] / sum, current_data[3] / sum])
		fixed_data.append(team_fixed)
	return fixed_data


def sum_tasks(data):
	fixed_data = list()
	for team_data in data:
		task = None
		current_data = list()
		team_fixed = list()

		for row in team_data:
			if row[1] == task:
				current_data[2] += row[3]
				current_data[3] += row[4]
			else:
				if task is not None:
					team_fixed.append([row[0], current_data[1], 'na', current_data[2], current_data[3]])

				current_data = [row[0], row[1], row[3], row[4]]
				task = row[1]

		team_fixed.append([current_data[0], current_data[1], 'na', current_data[2], current_data[3]])
		fixed_data.append(team_fixed)
	return fixed_data


#Configure data for visual
def flatten_success(data):
	flat_list = list()
	for sublist in data:
		for item in sublist:
			flat_list.append([item[0], item[1], item[3]])
	return flat_list


def flatten_attempt(data):
	flat_list = list()
	for sublist in data:
		for item in sublist:
			flat_list.append([item[0], item[1], item[4]])
	return flat_list


#Holoviews generation
def hv_table(data, label='Successes'):
	return hv.Table(data, "Team", ['Task', label])


def hv_stack(data, label='Successes', width=400, height=400):
	return hv.Bars(data, ["Team", "Task"], label).opts(plot=dict(tools=['hover'], stack_index=1, legend_position="top", width=width, height=height))


def hv_bar(data, label='Successes', width=400, height=400):
	return hv.Bars(data, ["Team", "Task"], label).opts(plot=dict(tools=['hover'], legend_position="top", xrotation=45, width=width, height=height))

def hv_box(data, label='Successes', width=400, height=400):
	return hv.BoxWhisker(data, ["Team", "Task"], label).opts(plot=dict(tools=['hover'], legend_position="top", xrotation=45, width=width, height=height))


def save_view(view, name):
	renderer = hv.renderer('bokeh')
	# Convert to bokeh figure then save using bokeh
	plot = renderer.get_plot(view).state

	from bokeh.io import output_file, save, show
	save(plot, config.web_data(name + '.html'), title=name)


def test_output(data):
	total = flatten_success(data)
	avg = flatten_success(average_tasks(data))
	avg_att = flatten_attempt(average_tasks(data))

	plot = hv_table(total) + hv_stack(avg) + hv_bar(avg) + hv_box(total)
	save_view(plot, 'test')