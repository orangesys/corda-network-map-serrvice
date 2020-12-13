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
import 'whatwg-fetch';
import React from 'react'
import {compose, withHandlers, withProps, withState} from "recompose";
import {GoogleMap, Marker, withGoogleMap, withScriptjs} from "react-google-maps";
import {MarkerClusterer} from "react-google-maps/lib/components/addons/MarkerClusterer";
import myMapStyle from 'mapStyle.json';
import clusterStyle from 'clusterStyle.json';


const MapWithAMarkerClusterer = compose(
  withProps({
    googleMapURL: "https://maps.googleapis.com/maps/api/js?key=AIzaSyC4QrPK-xamnJwHo-CFW0XzeDj4INbsQYU&v=3.exp&libraries=geometry,drawing,places",
    loadingElement: <div className='map-load-component' />,
    containerElement: <div className='map-container-component' />,
    mapElement: <div className='map-component' />,
    imagePath: "png/cf.png"
  }),
  withState('zoom', 'onZoomChange', 2 ),
  withHandlers((props) => {
    const refs = {
      map: undefined
    }

    return {
      onMarkerClustererClick: () => (markerClusterer) => {
        //const clickedMarkers = markerClusterer.getMarkers()
        // console.log(`Current clicked markers length: ${clickedMarkers.length}`)
        // console.log(clickedMarkers)
        console.log(markerClusterer)
      },
      setZoom: () => {},
      onMapMounted: () => ref => {
        refs.map = ref
      },
      onZoomChanged: ({ onZoomChange }, zoom)  => () => {
        onZoomChange(refs.map.getZoom());
      }
    }
  }),
  withScriptjs,
  withGoogleMap
)( props => {
  return (
  <GoogleMap
    zoom={props.zoom}
    defaultCenter={{ lat: 53, lng: 0 }}
    defaultOptions={
      { 
        styles: myMapStyle,
        fullscreenControl: false,
        mapTypeControl: false,
        maxZoom: 10,
        minZoom: 2,
        streetViewControl: false
      }
    }
    ref={props.onMapMounted}
    onZoomChanged={props.onZoomChanged}
  >
    <MarkerClusterer
      averageCenter
      clusterClass="cluster cluster-component"
      enableRetinaIcons      
      gridSize={60}
      maxZoom={10}
      minimumClusterSize={1}
      onClick={props.onMarkerClustererClick}
      styles={clusterStyle}
      anch
    >
      {props.markers.map( (marker, index) => (
        <Marker
          key={index}
          position={{ lat: marker.lat, lng: marker.lng }}
          icon={"png/node.png"}
        />
      ))}
    </MarkerClusterer>
  </GoogleMap>
  );
});

const DemoApp = (props) => { 
  return (
    <div className="row grid-responsive">
			<div className="column page-heading">
				<div className="large-card">
          <MapWithAMarkerClusterer markers={props.nodes} />
        </div>
      </div>
    </div>
  )
}

export default DemoApp
