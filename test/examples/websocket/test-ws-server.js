var connection = new WebSocket('ws://127.0.0.1:30024');
connection.onerror = function(e) {
	console.log('error');
	console.log(e);
};
connection.onmessage = function(e) {
	console.log('message');
	console.log(e);
};
connection.onclose = function(e) {
	console.log('closed');
	console.log(e);
};
connection.onopen = function(e) { 
	console.log('open');
	console.log(e);
	sendMsg();
};

function sendMsg(){
	var counter = 10;
	var handle = setInterval(function(){
		connection.send(makeid(128));
		if(--counter < 0){
			connection.close();
			clearInterval(handle);
		}
	},250);

	function makeid(length) {
	   var result           = '';
	   var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	   var charactersLength = characters.length;
	   for ( var i = 0; i < length; i++ ) {
	      result += characters.charAt(Math.floor(Math.random() * charactersLength));
	   }
	   return result;
	}	
}
