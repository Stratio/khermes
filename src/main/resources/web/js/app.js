window.onload = function() {
  setupWebSocket(wsURL("input"));
  setupWebSocket(wsURL("output"));

  function setupWebSocket(endpoint) {
    if(window.ws) {
      window.ws.close();
    }

    var ws = new WebSocket(endpoint)

    ws.onopen = function(event) {
      console.info("Connected to the server")
    };

    ws.onmessage = function(event) {
      console.log(event);
      createEventDiv(event);
    };

    ws.onclose = function() {
      console.info("Disconnected to the server");
      setupWebSocket(this.url)
    };

    window.ws = ws;
  }

  function wsURL(path) {
    var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
    var url = protocol + location.host;
    if(location.hostname === 'localhost') {
      url += '/' + location.pathname.split('/')[1];
    } else {
      url += '/';
    }
    return url + path;
  };


  function createEventDiv(obj) {
      var newDiv = "";
      newDiv += '<div class="alert alert-info" role="alert">';
      newDiv += '<p>' + obj.data + '</p>';
      newDiv += "</div>";
      $("div#content").prepend(newDiv);
  };

};