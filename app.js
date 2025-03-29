const http = require('http');

var requestOptions = {
    socketPath: '/var/run/docker.sock'
};

let containerId = "containerId2";
let apiOptions = requestOptions;
apiOptions.path = `/containers/${containerId}/start`;
apiOptions.method = 'POST';
apiOptions.headers = {
    'Content-Type': 'application/json',
    'Content-Length': 0
};
let data = '';
const apiReq = http.request(apiOptions, (apiRes) => {
    console.log(`${apiRes.statusCode} - ${apiOptions.path}`);
    apiRes.on('data', d => {
        data += d.toString();
    });
    apiRes.on('end', () => {
        console.log("aaaaaa")
    });
});
apiReq.on('error', err => {
    console.error(err)
});
apiReq.end();