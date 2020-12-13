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
const API_KEY = 'AIzaSyC4QrPK-xamnJwHo-CFW0XzeDj4INbsQYU';
const url = new URL('https://maps.googleapis.com/maps/api/geocode/json')

export async function lookupLatLong(location){
  let params = {
    address: `${location.city}`,
    key: API_KEY,
    region: location.country.toLowerCase()
  }
  url.search = new URLSearchParams(params);

  const response = await fetch(url);
  let status = await response.status;
  if(status === 200) {
    let locations = await response.json();
    return locations.results[0].geometry.location;
  }
  else{
    return response.statusText;
  }
}

export async function nodesLatLong(uniqueLocations){
  const locMap = new Map();
  for(let i in uniqueLocations){
    let tempLatLong = await lookupLatLong(uniqueLocations[i]);
    locMap.set(
      `${uniqueLocations[i].city}${uniqueLocations[i].country}`, 
      {
        lat: tempLatLong.lat, 
        lng: tempLatLong.lng
      } 
    )
  }
  return locMap;
}