function setEvent() {
					var event = document.getElementById('event').value;
					var year = document.getElementById('year').value;
					window.location = "/event/eventcurrent?event=" + event + "&year=" + year;
}
function getEvent() {
					var event = document.getElementById('event').value;
					var year = document.getElementById('year').value;
					window.location = "/event/eventfind?event=" + event + "&year=" + year;
}