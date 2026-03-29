const http = require('http');
const fs = require('fs');
const req = http.request({
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {'Content-Type': 'application/json'}
}, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    const token = JSON.parse(data).data.accessToken;
    console.log("Got token, calling fleet/filter...");
    
    const filterReq = http.request({
        hostname: 'localhost',
        port: 8080,
        path: '/api/testdrive/fleet/filter',
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token}
    }, res2 => {
        let ret = '';
        res2.on('data', chunk => ret += chunk);
        res2.on('end', () => {
            fs.writeFileSync('fleet_error.txt', 'Status: ' + res2.statusCode + '\n' + ret, 'utf8');
            console.log("Status:", res2.statusCode);
            console.log("Response:", ret.substring(0, 500));
        });
    });
    filterReq.write(JSON.stringify({filters: [], sorts: [{field: "id", direction: "DESC"}], page: 0, size: 10}));
    filterReq.end();
  });
});
req.write(JSON.stringify({username: 'admin', password: 'Admin@123'}));
req.end();
