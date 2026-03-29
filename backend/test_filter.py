import urllib.request, json

req1 = urllib.request.Request('http://localhost:8080/api/auth/login', data=json.dumps({'username': 'admin', 'password': 'Admin@123'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
token = json.loads(urllib.request.urlopen(req1).read())['data']['token']

req2 = urllib.request.Request('http://localhost:8080/api/sales/leads/filter', data=json.dumps({'filters': [{'field': 'branch.id', 'operator': 'EQUAL', 'value': '2'}], 'page': 0, 'size': 20}).encode('utf-8'), headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token})

try:
    urllib.request.urlopen(req2)
    print("Success")
except urllib.error.HTTPError as e:
    print("HTTP ERROR:", e.code)
    print(e.read().decode('utf-8'))
