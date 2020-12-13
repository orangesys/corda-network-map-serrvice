/*
 *   Copyright 2018, Cordite Foundation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
import {checkToken} from 'scripts/jwtProcess';

const url = document.baseURI;

export async function login(loginData) {
  const response = await fetch(`${url}/admin/api/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(loginData)
  });
  let status = await response.status;
  if (status === 200) {
    sessionStorage["corditeAccessToken"] = await response.text();
  } else {
    console.log(response);
  }
  return response;
}

export async function checkAuth() {
  let status = 403
  const token = sessionStorage['corditeAccessToken'];
  if (token && checkToken(token)) {
    status = 200;
  }
  return status;
}

export async function getNodes() {
  const token = sessionStorage["corditeAccessToken"];
  const response = await fetch(`${url}/admin/api/nodes`, {
    method: 'GET',
    headers: {
      'accept': 'application/json',
      "Authorization": `Bearer ${token}`
    }
  })
  let nodes = await response.json();
  return nodes;
}

export async function getNotaries() {
  const response = await fetch(`${url}/admin/api/notaries`, {
    method: 'GET',
    headers: {
      'accept': 'application/json',
      "Authorization": `Bearer ${sessionStorage["corditeAccessToken"]}`
    }
  })
  let notaries = await response.json();
  return notaries;
}

export async function getBuildProperties() {
  const response = await fetch(`${url}/admin/api/build-properties`, {
    method: 'GET',
    headers: {
      'accept': 'application/json'
    }
  });
  let properties = await response.json();
  return properties;
}

export async function getBraidAPI() {
  const response = await fetch(`${url}/braid/api`, {
    method: 'GET',
    headers: {
      'accept': 'application/json',
      "Authorization": `Bearer ${sessionStorage["corditeAccessToken"]}`
    }
  })
  let braidCode = await response.json();
  return braidCode;
}

export async function deleteNodes(nodeKey) {
  const response = await fetch(`${url}/admin/api/nodes/${nodeKey}`,
    {
      method: 'DELETE',
      headers: {
        'accept': 'application/json',
        "Authorization": `Bearer ${sessionStorage["corditeAccessToken"]}`
      }
    });
  return response;
}

