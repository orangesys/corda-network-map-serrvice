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
const getJWTExpiryDate = (token) => {
  let jwtArray = token.split('.');
  let result = JSON.parse(atob(jwtArray[1]));
  return result.exp;
}

export const checkToken = (token) => {
  let unixTime = Date.now();
  return (Math.floor(unixTime/1000) <= getJWTExpiryDate(token));
}