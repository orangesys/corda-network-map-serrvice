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
import PropTypes from 'prop-types'

export const Table = (props) => {
  const { headersList, rowData, sortTable, admin, toggleModal } = props;
  return(
    <div className="row grid-responsive mt-2">
      <div className="column ">
        <div className="card" >
          <div className="card-block">
            <div className="table-component">
              <table>
                <TableHead 
                  headersList={headersList}
                  sortTable={sortTable}
                  admin={admin}
                />                  
                <TableBody 
                  headersList={headersList}
                  rowData={rowData}
                  toggleModal={toggleModal}
                  admin={admin}
                />  
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

const TableHead = (props) => {
  const { headersList, sortTable, admin } = props;
  const sortCol = (e) => {
    sortTable(e)
  }
  return(
    <thead>
      <tr>
        <th>Node</th>
        {
          Array.from(headersList.values()).map((h, index) => (
            <th key={index} data-header={h} onClick={e => sortCol(e)}>{h}<img src='png/sort.png' data-header={h}/></th>
          ))
        }
        { admin ? <th>Controls</th> : null }
      </tr>
    </thead>
  )
}

const TableBody = (props) => {
  const { headersList, rowData, toggleModal, admin } = props;
  let tr = rowData.map((node, index) => {
      return ( 
      <TableRow 
        key={index} 
        node={node} 
        headersList={headersList} 
        toggleModal={toggleModal}
        admin={admin} /> );
  })  
  return (
      <tbody>{tr}</tbody>
  );
}

const TableRow = (props) => {
  const { node, headersList, toggleModal, admin } = props;
  const valueArray = [];

  headersList.forEach((value, key) => {
    if (node.hasOwnProperty(key)) {
      valueArray.push(node[key]);
    } else {
      valueArray.push("");
    }
  });

  return (
    <tr className="table-row-component">
      <td><TableTooltip tooltip={node.nodeKey}/>{node.nodeKey.slice(0, 3) + '...' + node.nodeKey.substr(-3)}</td>
      {
        valueArray.map((value, index) => {
          let td
          typeof value === "boolean" ?  
            td =  <td className={value.toString()} key={index}></td> 
            : 
            td =  <td key={index}>{value.toString()}</td>         
          return td;
        })
      }
      { (admin) ? 
        <td key={9}>
          <button 
            className='wibble'>
              <em className="fa fa-trash" data-link="delete" onClick={ e => { toggleModal(e, node) }}></em>
          </button> 
        </td> : null
      }
    </tr>
  );
};


const TableTooltip = (props) => {
  return (
    <div className='table-tooltip-component'>{props.tooltip}</div>
  );
};

Table.propTypes = {
  headersList: PropTypes.object.isRequired,
  rowData: PropTypes.array.isRequired
}

TableHead.propTypes = {
  headersList: PropTypes.object.isRequired
}

TableBody.propTypes = {
  rowData: PropTypes.array.isRequired
}

TableRow.propTypes = {
  nodes: PropTypes.object
}

