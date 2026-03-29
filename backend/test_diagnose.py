import urllib.request, json, sys

try:
    req1 = urllib.request.Request('http://localhost:8080/api/auth/login', data=json.dumps({'username': 'admin', 'password': 'Admin@123'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
    resp1 = urllib.request.urlopen(req1).read()
    token = json.loads(resp1)['data']['accessToken']
    
    # Testing Leads filter with branch.id
    payload = {'filters': [{'field': 'branch.id', 'operator': 'EQUAL', 'value': '2'}], 'page': 0, 'size': 20}
    req2 = urllib.request.Request('http://localhost:8080/api/sales/leads/filter', data=json.dumps(payload).encode('utf-8'), headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token})
    
    try:
        urllib.request.urlopen(req2)
        print("Success")
    except urllib.error.HTTPError as he:
        body = he.read().decode('utf-8')
        error_json = json.loads(body)
        print("Detailed Error Message:", error_json.get('error', {}).get('message', 'No message'))
        print("Detailed Error Trace:", error_json.get('error', {}).get('trace', 'No trace'))
        
except Exception as e:
    print("General Exception:", str(e))
