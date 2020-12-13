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
import React from 'react'
import PropTypes from 'prop-types';
import {Nav} from 'components/Nav/Nav'
import {Table} from 'components/Table/Table';
import DemoMap from 'components/Map/MyMap';
import {Metrics} from 'components/Metrics/Metrics';

export const Home = (props) => {
  const { nodes, notaries, headersList, sortTable } = props
  return (
    <section className='home-component'>
      <DemoMap nodes={nodes}/>          
      <Metrics 
        nodes={nodes}
        notaries={notaries}
      />
      <Table 
        headersList={headersList}
        rowData={nodes}
        sortTable={sortTable}
        toggleModal={props.toggleModal}
        admin={props.admin}
      />
    </section>
  )
}

Home.propTypes = {
  headersList: PropTypes.object.isRequired,
  nodes: PropTypes.array.isRequired,
  notaries: PropTypes.array.isRequired
}

/*
<DemoMap nodes={nodes}/>
<Metrics 
  nodes={nodes}
  notaries={notaries}
/>

*/