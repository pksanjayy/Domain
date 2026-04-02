import urllib.request
import urllib.error
import json

def api_call(url, method='GET', data=None):
    if data:
        data = json.dumps(data).encode('utf-8')
    req = urllib.request.Request(f'http://localhost:8080/api{url}', method=method, data=data)
    req.add_header('Content-Type', 'application/json')
    try:
        response = urllib.request.urlopen(req)
        return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print(f"Error {e.code} on {method} {url}: {e.read().decode('utf-8')}")
        return None

# 1. Create Customer
cust = api_call('/customers', 'POST', {"name":"John Doe", "mobile":"9998887776", "branchId":1})
if cust: print("Customer created:", cust['data']['id'])
else: exit(1)

# 2. Create Fleet
fleet = api_call('/testdrive/fleet', 'POST', {"fleetId":"FL-999", "vin":"VIN999", "brand":"Hyundai", "model":"i20", "variant":"Asta", "fuelType":"PETROL", "transmission":"MANUAL", "branchId":1, "registrationNumber":"KA019999", "status":"AVAILABLE", "currentOdometer":0})
if fleet: print("Fleet created:", fleet['data']['id'])
else: exit(1)

# 3. Create Booking
booking = api_call('/testdrive/bookings', 'POST', {"customerId":cust['data']['id'], "fleetId":fleet['data']['id'], "bookingDate":"2024-03-24", "testDriveDate":"2024-03-25", "timeSlot":"10:00:00", "status":"BOOKED"})
if booking:
    b_id = booking['data']['id']
    print("Booking created:", b_id)
    # 4. GET Booking
    get_res = api_call(f'/testdrive/bookings/{b_id}', 'GET')
    print("GET success:", get_res and get_res.get('success'))
    
    # 5. UPDATE Booking
    update_data = {"customerId":cust['data']['id'], "fleetId":fleet['data']['id'], "bookingDate":"2024-03-24", "testDriveDate":"2024-03-25", "timeSlot":"11:00:00", "status":"CONFIRMED"}
    up_res = api_call(f'/testdrive/bookings/{b_id}', 'PUT', update_data)
    print("UPDATE success:", up_res and up_res.get('success'))
