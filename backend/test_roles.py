import urllib.request, json, sys

def check_role(username, role_name):
    try:
        # Login
        url = 'http://localhost:8080/api/auth/login'
        data = json.dumps({'username': username, 'password': 'Admin@123'}).encode('utf-8')
        req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
        resp = urllib.request.urlopen(req)
        user_profile = json.loads(resp.read().decode('utf-8'))['data']['user']
        
        print(f"\n--- Checking Menus for {username} ({role_name}) ---")
        found_td = False
        for menu in user_profile['menus']:
            if menu['name'] == 'Test Drive':
                found_td = True
                print(f"Parent Menu 'Test Drive' found: OK")
                child_names = [c['name'] for c in menu['children']]
                print(f"Sub-menus: {child_names}")
                
        if not found_td:
            print("ERROR: Parent menu 'Test Drive' NOT found!")
            
    except Exception as e:
        print(f"FAILED for {username}: {str(e)}")

# Wait for backend (implicit in run_command)
check_role('sales_hyd_1', 'SALES_CRM_EXEC')
check_role('workshop_hyd_1', 'WORKSHOP_EXEC')
