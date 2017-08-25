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
jQuery(document).ready(function($) {
    $('body').terminal(function(command, term) {
        if (command == 'help') {
            term.echo("available commands are ls, create kafka-config, create twirl-template, create generator-config, create avro-config, start, stop, show kafka-config, show generator-config, show avro-config, show twirl-template");
        } else if (command == 'ls'){
            sendMessage('[command]\nls');
        } else if (command == 'create kafka-config') {
            createConfig('kafka-config',term)
        } else if (command == 'create twirl-template') {
            createConfig('twirl-template',term)
        } else if (command == 'create generator-config') {
            createConfig('generator-config',term)
        } else if (command == 'create avro-config') {
            createConfig('avro-config',term)
        }else if (command.startsWith('start')) {
            start(term)
        }else if (command.startsWith('stop')) {
            stop(term)
        }
        else if (command == 'show kafka-config') {
            sendMessage('[command]\nshow kafka-config')
        }
         else if (command == 'show generator-config') {
            sendMessage('[command]\nshow generator-config')
        }
        else if (command == 'show avro-config') {
            sendMessage('[command]\nshow avro-config')
        }
        else if (command == 'show twirl-template') {
            sendMessage('[command]\nshow twirl-template')
        }
        else if (command.startsWith('show kafka-config ')) {
            sendMessage('[command]\nshow kafka-config\n[name]\n'+command.split(" ").pop(2)+'\n')
        }
        else if (command.startsWith('show generator-config')) {
            sendMessage('[command]\nshow generator-config\n[name]\n'+command.split(" ").pop(2)+'\n')
        }
        else if (command.startsWith('show avro-config')) {
            sendMessage('[command]\nshow avro-config\n[name]\n'+command.split(" ").pop(2)+'\n')
        }
        else if (command.startsWith('show twirl-template')) {
            sendMessage('[command]\nshow twirl-template\n[name]\n'+command.split(" ").pop(2)+'\n')
        }
        else {
            term.echo("unknown command " + command);
        }
    }, {
        greetings: '\n' +
        '|╦╔═┬ ┬┌─┐┬─┐┌┬┐┌─┐┌─┐\n'+
        '|╠╩╗├─┤├┤ ├┬┘│││├┤ └─┐\n'+
        '|╩ ╩┴ ┴└─┘┴└─┴ ┴└─┘└─┘ Powered by Stratio (www.stratio.com)\n',
        prompt: 'khermes> ',
        onBlur: function() {
            // prevent loosing focus
            return false;
        },
        onInit: function(term) {
            setupWebSocket(wsURL("input"), "input", term);
            setupWebSocket(wsURL("output"), "output", term);
        }
    });
});


function createConfig(commandName, term){
    var name = '';
    var config = '';
    term.push(function(nameTemplate, term) {
        name = nameTemplate;
        term.push(function(nameConfig, term) {
            config = nameConfig;
            sendMessage('[command]\ncreate '+commandName+'\n[name]\n'+name+'\n[content]\n'+config+'\n');
            //Back the prompt to the original level.
            term.pop();
            term.pop();
        }, {
            prompt: 'khermes> '+commandName+'> Please introduce the '+commandName+'> \n',
            name: commandName});
    },
    {
        prompt: 'khermes> '+commandName+'> Please introduce the '+commandName+' name> ',
        name: commandName+'-name'});
}

function start(term){
      var twirl = '';
      var kafka = '';
      var generator = '';
      var avro = '';
      var nodes = '';
    term.push(function(twirlTemplate, term) {
        twirl = twirlTemplate;
        term.push(function(kafkaConfig, term) {
            kafka = kafkaConfig;
             term.push(function(generatorConfig, term) {
                  generator = generatorConfig;
                      term.push(function(avroConfig, term) {
                                 avro = avroConfig;
                                term.push(function(nodeIds, term) {
                                           nodes = nodeIds;
                                           sendMessage('[command]\nstart\n[twirl-template]\n'+twirl+
                                                               '\n[kafka-config]\n'+kafka+
                                                               '\n[generator-config]\n'+generator+
                                                               '\n[avro-config]\n'+avro+
                                                               '\n[node-ids]\n'+nodes+'\n');
                                           //Back the prompt to the original level.
                                           term.pop();
                                           term.pop();
                                           term.pop();
                                           term.pop();
                                           term.pop();
                                       }, {
                                           prompt: 'khermes> start > Please introduce the node-ids> \n',
                                           name: 'startNodeIds'});
                             }, {
                                 prompt: 'khermes> start > Please introduce the avro-config name> \n',
                                 name: 'startAvroConfig'});
                    }, {
                        prompt: 'khermes> start > Please introduce the generator-config name> \n',
                        name: 'startGeneratorConfig'});

        }, {
            prompt: 'khermes> start > Please introduce the kafka-config name> \n',
            name: 'startKafkaConfig'});
    },
    {
        prompt: 'khermes> start > Please introduce the twirl-template name> ',
        name: 'startTwirlTemplate'});
}

function stop(term){
      var nodes = '';
    term.push(function(nodeIds, term) {
        nodes = nodeIds;
      sendMessage('[command]\nstop\n[node-ids]\n'+nodes+'\n');
      term.pop();
    },
    {
        prompt: 'khermes> stop > Please introduce the node-Ids name> ',
        name: 'stopNodeIds'});
}

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
    if (data.indexOf("|") != -1)
       term.echo(parseLs(event.data))
    else
       term.echo(parseOkResponse(event.data));
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

function parseOkResponse(data) {
    var response = JSON.parse(data);
    return "Command result: "+ response.value;
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