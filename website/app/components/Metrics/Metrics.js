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
import React from 'react';

export const Metrics = (props) => {
  return(
    <div className="row grid-responsive metrics-component">
      <Metric nodes={props.nodes} />
      <Metric notaries={props.notaries} />
    </div>
  );
}

const Metric = (props) => {
  let node = Object.keys(props)[0];
  let additions = '';
  if(node === 'notaries'){
    additions = 'Node';
  }
  return(
    <div className="m-component column">
      <div className="card">
        <div className="card-title">
          <h2 className="float-left">{node + " " + additions + " #"}</h2>
          <div className="badge background-primary float-right">{props[node].length}</div>
          <div className="clearfix"></div>
        </div>
      </div>
    </div>
  );
}

