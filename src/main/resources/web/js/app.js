/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the terms of the License for more details.
 *
 * SPDX-License-Identifier:  Apache-2.0.
 */
setupWebSocket(wsURL("input"), "input");
setupWebSocket(wsURL("output"), "output");

function setupWebSocket(endpoint, name) {
  if(window[name]) {
    window[name].close();
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

  window[name] = ws;
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
  newDiv += '<div class="feedback alert alert-info" role="alert" style="margin-top: 20px">';
  newDiv += '<p>' + obj.data + '</p>';
  newDiv += "</div>";
  $(".feedback" ).remove();
  $("div#content").append(newDiv);
};

function send() {
  var messageToSend = document.getElementById('command').value;
  document.getElementById('command').value = '';
  window["input"].send(messageToSend);
};