function setupWebSocket(endpoint, name, term) {

  if(window[name]) {
    window[name].close();
  }

  var ws = new WebSocket(endpoint)

  ws.onopen = function(event) {
    console.info("Connected to the server")
  };

  ws.onmessage = function(event) {
    console.log(event);
    data = event.data;
    if (data.indexOf("value") != -1)
       term.echo(parseLs(event.data))
    else
       term.echo(event.data);
  };

  ws.onclose = function() {
    console.info("Disconnected from the server");
    setupWebSocket(this.url)
  };

  window[name] = ws;
}

function wsURL(path) {
  var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
  var url = protocol + location.host + '/';
  return url + path;
};

function parseLs(data) {
    header = "Node Id                                   | Status  ";
    separator = "-------------------------------------------------";
    var response = JSON.parse(data);
    return header + "\n" + separator + "\n"+ response.value;
}

function sendMessage(msg){
    if (window["input"].readyState === 1) {
        window["input"].send(msg);
    } else {
        // Wait until the state of the socket is not ready and send the message when it is...
        waitForSocketConnection(window["input"], function () {
            window["input"].send(msg);
        });
    }
}

function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                console.log("Connection is made")
                if(callback != null){
                    callback();
                }
                return;

            } else {
                console.log("wait for connection...")
                waitForSocketConnection(socket, callback);
            }

        }, 50); // wait 50 miliseconds for the connection...
}