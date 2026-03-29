import urllib.request, json, sys

try:
    req1 = urllib.request.Request('http://localhost:8080/api/auth/login', data=json.dumps({'username': 'admin', 'password': 'Admin@123'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
    resp1 = urllib.request.urlopen(req1).read()
    token = json.loads(resp1)['data']['accessToken']
    
    # Testing Booking filter with lead.branch.id
    payload = {'filters': [{'field': 'lead.branch.id', 'operator': 'EQUAL', 'value': '2'}], 'page': 0, 'size': 20}
    req2 = urllib.request.Request('http://localhost:8080/api/sales/bookings/filter', data=json.dumps(payload).encode('utf-8'), headers={'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token})
    
    try:
        resp2 = urllib.request.urlopen(req2)
        print("Booking Filter Success")
        print(resp2.read().decode('utf-8')[:200]) # Print snippet
    except urllib.error.HTTPError as he:
        print("BOOKING FILTER ERROR:", he.code)
        print(he.read().decode('utf-8'))
        
except Exception as e:
    print("Exception:", str(e))
