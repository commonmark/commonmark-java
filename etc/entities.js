// 1. curl -O "https://html.spec.whatwg.org/multipage/entities.json"
// 2. run this script with node

var fs = require('fs');
var data = JSON.parse(fs.readFileSync("entities.json"));

var result = "";
for (var key in data) {
  // exclude names not ending with ";" as per CommonMark spec
  if (!data.hasOwnProperty(key) || key.slice(-1) !== ";") {
    continue;
  }
  // special handling
  if (key === 'NewLine') {
    continue;
  }
  result += key.slice(1, -1) + '=' + data[key].characters + '\n';
}
fs.writeFileSync("entities.properties", result);
