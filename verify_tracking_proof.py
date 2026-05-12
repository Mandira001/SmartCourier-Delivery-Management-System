import json
import os
import urllib.request
import urllib.error
import uuid

GATEWAY_BASE = 'http://localhost:8080/gateway'
GATEWAY_ROOT = 'http://localhost:8080'
TRACKING_NUMBER = 'f61cc106-59c2-4f06-9a8b-ef47d0854a71'
USER_EMAIL = 'user@example.com'
USER_PASSWORD = 'UserPass123'


def request(method, url, headers=None, data=None):
    if headers is None:
        headers = {}
    if data is not None and 'Content-Type' not in headers:
        headers['Content-Type'] = 'application/json'
    body = None
    if data is not None:
        if isinstance(data, (bytes, bytearray)):
            body = data
        elif isinstance(data, str):
            body = data.encode('utf-8')
        else:
            body = json.dumps(data).encode('utf-8')
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.status, resp.read().decode('utf-8')
    except urllib.error.HTTPError as err:
        return err.code, err.read().decode('utf-8')
    except Exception as err:
        return 'ERR', str(err)


def login_user():
    status, body = request('POST', f'{GATEWAY_BASE}/auth/auth/login', data={'email': USER_EMAIL, 'password': USER_PASSWORD})
    print('LOGIN', status, body)
    if status != 200:
        raise SystemExit('Login failed')
    return body.strip().strip('"')


def build_multipart(fields, file_field, filename, content, content_type='text/plain'):
    boundary = '----WebKitFormBoundary' + uuid.uuid4().hex
    lines = []
    for name, value in fields.items():
        lines.append(f'--{boundary}')
        lines.append(f'Content-Disposition: form-data; name="{name}"')
        lines.append('')
        lines.append(value)
    lines.append(f'--{boundary}')
    lines.append(f'Content-Disposition: form-data; name="{file_field}"; filename="{filename}"')
    lines.append(f'Content-Type: {content_type}')
    lines.append('')
    if isinstance(content, str):
        lines.append(content)
    else:
        lines.append(content.decode('utf-8'))
    lines.append(f'--{boundary}--')
    body = '\r\n'.join(lines).encode('utf-8')
    content_type_header = f'multipart/form-data; boundary={boundary}'
    return body, content_type_header


def upload_document(token):
    filename = 'tracking_doc.txt'
    content = 'Tracking document upload test'
    body, ct = build_multipart({'trackingNumber': TRACKING_NUMBER}, 'file', filename, content)
    headers = {'Authorization': 'Bearer ' + token, 'Content-Type': ct}
    url = f'{GATEWAY_BASE}/tracking/tracking/documents/upload?trackingNumber={TRACKING_NUMBER}'
    status, resp = request('POST', url, headers=headers, data=body)
    print('UPLOAD', status, resp)
    return status, resp


def get_documents(token):
    url = f'{GATEWAY_BASE}/tracking/tracking/documents/{TRACKING_NUMBER}'
    status, resp = request('GET', url, headers={'Authorization': 'Bearer ' + token})
    print('DOCS', status, resp)
    return status, resp


def download_document(token, doc_id):
    url = f'{GATEWAY_BASE}/tracking/tracking/documents/download/{doc_id}'
    status, resp = request('GET', url, headers={'Authorization': 'Bearer ' + token})
    print('DOWNLOAD', status, resp[:200])
    return status, resp


def save_proof(token):
    payload = {'trackingNumber': TRACKING_NUMBER, 'receiverName': 'Test Receiver', 'proofImage': 'proof-image-path.jpg'}
    status, resp = request('POST', f'{GATEWAY_BASE}/tracking/tracking/proof', headers={'Authorization': 'Bearer ' + token}, data=payload)
    print('SAVE PROOF', status, resp)
    return status, resp


def get_proof(token):
    url = f'{GATEWAY_BASE}/tracking/tracking/{TRACKING_NUMBER}/proof'
    status, resp = request('GET', url, headers={'Authorization': 'Bearer ' + token})
    print('GET PROOF', status, resp)
    return status, resp


def check_swagger(token=None):
    urls = [
        f'{BASE}/swagger-ui.html',
        f'{BASE}/auth/v3/api-docs',
        f'{BASE}/deliveries/v3/api-docs',
        f'{BASE}/tracking/v3/api-docs',
        f'{BASE}/admin/v3/api-docs'
    ]
    for url in urls:
        headers = {'Authorization': 'Bearer ' + token} if token else {}
        status, resp = request('GET', url, headers=headers)
        print('SWAGGER', url, status, resp[:200] if isinstance(resp, str) else resp)


if __name__ == '__main__':
    token = login_user()
    upload_document(token)
    docs_status, docs_body = get_documents(token)
    if docs_status == 200:
        try:
            docs = json.loads(docs_body)
            if docs and isinstance(docs, list) and 'id' in docs[0]:
                download_document(token, docs[0]['id'])
        except Exception as e:
            print('DOC PARSE ERROR', e)
    save_proof(token)
    get_proof(token)
    print('=== SWAGGER CHECK ===')
    check_swagger(token)
