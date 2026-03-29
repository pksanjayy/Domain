import urllib.request, json, sys

try:
    req1 = urllib.request.Request('http://localhost:8080/api/auth/login', data=json.dumps({'username': 'admin', 'password': 'Admin@123'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
    resp1 = urllib.request.urlopen(req1).read()
    token = json.loads(resp1)['data']['accessToken']
    
    req2 = urllib.request.Request('http://localhost:8080/api/sales/leads/filter', data=json.dumps({'filters': [{'field': 'branch.id', 'operator': 'EQUAL', 'value': '2'}], 'page': 0, 'size': 20}).encode('utf-8'), headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token})
    resp2 = urllib.request.urlopen(req2).read()
    print("Filter Response:", resp2)
except urllib.error.HTTPError as e:
    print("HTTP ERROR:", e.code)
    print(e.read().decode('utf-8'))
except Exception as e:
    print("Exception:", str(e))
