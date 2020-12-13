(function() {
  'use strict';

  var globals = typeof global === 'undefined' ? self : global;
  if (typeof globals.require === 'function') return;

  var modules = {};
  var cache = {};
  var aliases = {};
  var has = {}.hasOwnProperty;

  var expRe = /^\.\.?(\/|$)/;
  var expand = function(root, name) {
    var results = [], part;
    var parts = (expRe.test(name) ? root + '/' + name : name).split('/');
    for (var i = 0, length = parts.length; i < length; i++) {
      part = parts[i];
      if (part === '..') {
        results.pop();
      } else if (part !== '.' && part !== '') {
        results.push(part);
      }
    }
    return results.join('/');
  };

  var dirname = function(path) {
    return path.split('/').slice(0, -1).join('/');
  };

  var localRequire = function(path) {
    return function expanded(name) {
      var absolute = expand(dirname(path), name);
      return globals.require(absolute, path);
    };
  };

  var initModule = function(name, definition) {
    var hot = hmr && hmr.createHot(name);
    var module = {id: name, exports: {}, hot: hot};
    cache[name] = module;
    definition(module.exports, localRequire(name), module);
    return module.exports;
  };

  var expandAlias = function(name) {
    return aliases[name] ? expandAlias(aliases[name]) : name;
  };

  var _resolve = function(name, dep) {
    return expandAlias(expand(dirname(name), dep));
  };

  var require = function(name, loaderPath) {
    if (loaderPath == null) loaderPath = '/';
    var path = expandAlias(name);

    if (has.call(cache, path)) return cache[path].exports;
    if (has.call(modules, path)) return initModule(path, modules[path]);

    throw new Error("Cannot find module '" + name + "' from '" + loaderPath + "'");
  };

  require.alias = function(from, to) {
    aliases[to] = from;
  };

  var extRe = /\.[^.\/]+$/;
  var indexRe = /\/index(\.[^\/]+)?$/;
  var addExtensions = function(bundle) {
    if (extRe.test(bundle)) {
      var alias = bundle.replace(extRe, '');
      if (!has.call(aliases, alias) || aliases[alias].replace(extRe, '') === alias + '/index') {
        aliases[alias] = bundle;
      }
    }

    if (indexRe.test(bundle)) {
      var iAlias = bundle.replace(indexRe, '');
      if (!has.call(aliases, iAlias)) {
        aliases[iAlias] = bundle;
      }
    }
  };

  require.register = require.define = function(bundle, fn) {
    if (bundle && typeof bundle === 'object') {
      for (var key in bundle) {
        if (has.call(bundle, key)) {
          require.register(key, bundle[key]);
        }
      }
    } else {
      modules[bundle] = fn;
      delete cache[bundle];
      addExtensions(bundle);
    }
  };

  require.list = function() {
    var list = [];
    for (var item in modules) {
      if (has.call(modules, item)) {
        list.push(item);
      }
    }
    return list;
  };

  var hmr = globals._hmr && new globals._hmr(_resolve, require, modules, cache);
  require._cache = cache;
  require.hmr = hmr && hmr.wrap;
  require.brunch = true;
  globals.require = require;
})();

(function() {
var global = typeof window === 'undefined' ? this : window;
var process;
var __makeRelativeRequire = function(require, mappings, pref) {
  var none = {};
  var tryReq = function(name, pref) {
    var val;
    try {
      val = require(pref + '/node_modules/' + name);
      return val;
    } catch (e) {
      if (e.toString().indexOf('Cannot find module') === -1) {
        throw e;
      }

      if (pref.indexOf('node_modules') !== -1) {
        var s = pref.split('/');
        var i = s.lastIndexOf('node_modules');
        var newPref = s.slice(0, i).join('/');
        return tryReq(name, newPref);
      }
    }
    return none;
  };
  return function(name) {
    if (name in mappings) name = mappings[name];
    if (!name) return;
    if (name[0] !== '.' && pref) {
      var val = tryReq(name, pref);
      if (val !== none) return val;
    }
    return require(name);
  }
};
require.register("clusterStyle.json", function(exports, require, module) {
module.exports = [
  {   
    "anchor": [25,25],
    "height": 26,
    "textColor": "#FFFFFF",
    "url": "png/ping.png",
    "width": 26
  },
  {   
    "height": 28,
    "textColor": "#FFFFFF",
    "url": "png/ping.png",
    "width": 28
  },
  {   
    "height": 33,
    "textColor": "#FFFFFF",
    "url": "png/ping.png",
    "width": 33
  },
  {   

    "height": 39,
    "textColor": "#FFFFFF",
    "url": "png/ping.png",
    "width": 39
  },
  {   
    "height": 45,
    "textColor": "#FFFFFF",
    "url": "png/ping.png",
    "width": 45
  }
];
});

require.register("components/DisplayBraid/DisplayBraid.js", function(exports, require, module) {
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.DisplayBraid = undefined;

var _stringify = require("babel-runtime/core-js/json/stringify");

var _stringify2 = _interopRequireDefault(_stringify);

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var DisplayBraid = exports.DisplayBraid = function DisplayBraid(props) {
  return _react2.default.createElement(
    "div",
    { className: "display-braid-component column" },
    _react2.default.createElement(
      "div",
      { className: "card" },
      _react2.default.createElement(
        "div",
        { className: "card-title" },
        _react2.default.createElement(
          "h2",
          null,
          "Braid API"
        ),
        _react2.default.createElement(
          "div",
          { className: "clearfix" },
          _react2.default.createElement(
            "pre",
            null,
            (0, _stringify2.default)(props.json, null, 2)
          )
        )
      )
    )
  );
}; /*
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
});

;require.register("components/Map/MyMap.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

require('whatwg-fetch');

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _recompose = require('recompose');

var _reactGoogleMaps = require('react-google-maps');

var _MarkerClusterer = require('react-google-maps/lib/components/addons/MarkerClusterer');

var _mapStyle = require('mapStyle.json');

var _mapStyle2 = _interopRequireDefault(_mapStyle);

var _clusterStyle = require('clusterStyle.json');

var _clusterStyle2 = _interopRequireDefault(_clusterStyle);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var MapWithAMarkerClusterer = (0, _recompose.compose)((0, _recompose.withProps)({
  googleMapURL: "https://maps.googleapis.com/maps/api/js?key=AIzaSyC4QrPK-xamnJwHo-CFW0XzeDj4INbsQYU&v=3.exp&libraries=geometry,drawing,places",
  loadingElement: _react2.default.createElement('div', { className: 'map-load-component' }),
  containerElement: _react2.default.createElement('div', { className: 'map-container-component' }),
  mapElement: _react2.default.createElement('div', { className: 'map-component' }),
  imagePath: "png/cf.png"
}), (0, _recompose.withState)('zoom', 'onZoomChange', 2), (0, _recompose.withHandlers)(function (props) {
  var refs = {
    map: undefined
  };

  return {
    onMarkerClustererClick: function onMarkerClustererClick() {
      return function (markerClusterer) {
        //const clickedMarkers = markerClusterer.getMarkers()
        // console.log(`Current clicked markers length: ${clickedMarkers.length}`)
        // console.log(clickedMarkers)
        console.log(markerClusterer);
      };
    },
    setZoom: function setZoom() {},
    onMapMounted: function onMapMounted() {
      return function (ref) {
        refs.map = ref;
      };
    },
    onZoomChanged: function onZoomChanged(_ref, zoom) {
      var onZoomChange = _ref.onZoomChange;
      return function () {
        onZoomChange(refs.map.getZoom());
      };
    }
  };
}), _reactGoogleMaps.withScriptjs, _reactGoogleMaps.withGoogleMap)(function (props) {
  return _react2.default.createElement(
    _reactGoogleMaps.GoogleMap,
    {
      zoom: props.zoom,
      defaultCenter: { lat: 53, lng: 0 },
      defaultOptions: {
        styles: _mapStyle2.default,
        fullscreenControl: false,
        mapTypeControl: false,
        maxZoom: 10,
        minZoom: 2,
        streetViewControl: false
      },
      ref: props.onMapMounted,
      onZoomChanged: props.onZoomChanged
    },
    _react2.default.createElement(
      _MarkerClusterer.MarkerClusterer,
      {
        averageCenter: true,
        clusterClass: 'cluster cluster-component',
        enableRetinaIcons: true,
        gridSize: 60,
        maxZoom: 10,
        minimumClusterSize: 1,
        onClick: props.onMarkerClustererClick,
        styles: _clusterStyle2.default,
        anch: true
      },
      props.markers.map(function (marker, index) {
        return _react2.default.createElement(_reactGoogleMaps.Marker, {
          key: index,
          position: { lat: marker.lat, lng: marker.lng },
          icon: "png/node.png"
        });
      })
    )
  );
}); /*
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


var DemoApp = function DemoApp(props) {
  return _react2.default.createElement(
    'div',
    { className: 'row grid-responsive' },
    _react2.default.createElement(
      'div',
      { className: 'column page-heading' },
      _react2.default.createElement(
        'div',
        { className: 'large-card' },
        _react2.default.createElement(MapWithAMarkerClusterer, { markers: props.nodes })
      )
    )
  );
};

exports.default = DemoApp;
});

;require.register("components/Metrics/Metrics.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Metrics = undefined;

var _keys = require('babel-runtime/core-js/object/keys');

var _keys2 = _interopRequireDefault(_keys);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Metrics = exports.Metrics = function Metrics(props) {
  return _react2.default.createElement(
    'div',
    { className: 'row grid-responsive metrics-component' },
    _react2.default.createElement(Metric, { nodes: props.nodes }),
    _react2.default.createElement(Metric, { notaries: props.notaries })
  );
}; /*
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


var Metric = function Metric(props) {
  var node = (0, _keys2.default)(props)[0];
  var additions = '';
  if (node === 'notaries') {
    additions = 'Node';
  }
  return _react2.default.createElement(
    'div',
    { className: 'm-component column' },
    _react2.default.createElement(
      'div',
      { className: 'card' },
      _react2.default.createElement(
        'div',
        { className: 'card-title' },
        _react2.default.createElement(
          'h2',
          { className: 'float-left' },
          node + " " + additions + " #"
        ),
        _react2.default.createElement(
          'div',
          { className: 'badge background-primary float-right' },
          props[node].length
        ),
        _react2.default.createElement('div', { className: 'clearfix' })
      )
    )
  );
};
});

;require.register("components/Modal/Modal.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.DeleteModal = exports.LogoutModal = exports.LoginModal = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Login = require('containers/Login/Login');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var loggingOut = function loggingOut(toggleModal) {
  sessionStorage.clear();
  toggleModal();
};

var LoginModal = exports.LoginModal = function LoginModal(props) {
  return _react2.default.createElement(
    'div',
    {
      className: 'modal-component ' + (props.style ? 'on' : ''),
      'data-link': 'default',
      onClick: function onClick(e) {
        return props.toggleModal(e);
      } },
    _react2.default.createElement(_Login.LoginContainer, {
      nmsLogin: props.nmsLogin,
      toggleModal: props.toggleModal,
      setAdmin: props.setAdmin })
  );
};

var LogoutModal = exports.LogoutModal = function LogoutModal(props) {
  return _react2.default.createElement(
    'div',
    {
      className: 'modal-component ' + (props.style ? 'on' : ''),
      'data-link': 'default',
      onClick: function onClick(e) {
        return props.toggleModal(e);
      } },
    _react2.default.createElement(
      'div',
      { className: 'lm-container' },
      _react2.default.createElement(
        'div',
        { className: 'lm-middle' },
        _react2.default.createElement(ModalTitle, null),
        _react2.default.createElement(ModalContent, null),
        _react2.default.createElement(ModalButtonGroup, {
          toggleModal: props.toggleModal,
          setAdmin: props.setAdmin
        })
      )
    )
  );
};

var DeleteModal = exports.DeleteModal = function DeleteModal(props) {
  return _react2.default.createElement(
    'div',
    {
      className: 'modal-component ' + (props.style ? 'on' : ''),
      'data-link': 'default',
      onClick: function onClick(e) {
        return props.toggleModal(e);
      } },
    _react2.default.createElement(
      'div',
      { className: 'lm-container' },
      _react2.default.createElement(
        'div',
        { className: 'lm-middle' },
        _react2.default.createElement(ModalTitleDelete, null),
        _react2.default.createElement(ModalContentDelete, { node: props.selectedNode }),
        _react2.default.createElement(
          'div',
          { className: 'lm-footer' },
          _react2.default.createElement(
            'button',
            {
              className: 'btn pull-right',
              'data-btn': true,
              onClick: function onClick(e) {
                props.deleteNode();props.toggleModal();
              } },
            'Yes'
          ),
          _react2.default.createElement(
            'button',
            {
              className: 'btn pull-right',
              'data-btn': 'cancel',
              onClick: function onClick(e) {
                return props.toggleModal();
              } },
            'No'
          )
        )
      )
    )
  );
};

var ModalTitleDelete = function ModalTitleDelete(props) {
  return _react2.default.createElement(
    'div',
    { className: 'lm-title' },
    _react2.default.createElement('span', { className: 'fa fa-trash' }),
    _react2.default.createElement(
      'strong',
      null,
      '     Delete Node'
    )
  );
};

var ModalTitle = function ModalTitle(props) {
  return _react2.default.createElement(
    'div',
    { className: 'lm-title' },
    _react2.default.createElement('span', { className: 'fa fa-sign-out' }),
    _react2.default.createElement(
      'strong',
      null,
      ' LOG OUT?'
    )
  );
};

var ModalContentDelete = function ModalContentDelete(props) {
  return _react2.default.createElement(
    'div',
    { className: 'lm-content' },
    _react2.default.createElement(
      'p',
      { className: 'node-id' },
      props.node.O
    ),
    _react2.default.createElement(
      'p',
      { className: 'node-id' },
      'for node ',
      props.node.nodeKey
    ),
    _react2.default.createElement(
      'p',
      null,
      'Are you sure you want to delete this node?'
    )
  );
};

var ModalContent = function ModalContent(props) {
  return _react2.default.createElement(
    'div',
    { className: 'lm-content' },
    _react2.default.createElement(
      'p',
      null,
      'Are you sure you want to log out?'
    ),
    _react2.default.createElement(
      'p',
      null,
      'Press No if you want to continue work. Press Yes to logout current user.'
    )
  );
};

var ModalButtonGroup = function ModalButtonGroup(props) {
  return _react2.default.createElement(
    'div',
    { className: 'lm-footer' },
    _react2.default.createElement(
      'button',
      {
        className: 'btn pull-right',
        'data-btn': true,
        onClick: function onClick(e) {
          props.setAdmin(false);loggingOut(props.toggleModal);
        } },
      'Yes'
    ),
    _react2.default.createElement(
      'button',
      {
        className: 'btn pull-right',
        'data-btn': 'cancel',
        onClick: function onClick(e) {
          return props.toggleModal();
        } },
      'No'
    )
  );
};
});

;require.register("components/Nav/Nav.js", function(exports, require, module) {
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Nav = undefined;

var _getPrototypeOf = require("babel-runtime/core-js/object/get-prototype-of");

var _getPrototypeOf2 = _interopRequireDefault(_getPrototypeOf);

var _classCallCheck2 = require("babel-runtime/helpers/classCallCheck");

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require("babel-runtime/helpers/createClass");

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require("babel-runtime/helpers/possibleConstructorReturn");

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require("babel-runtime/helpers/inherits");

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Nav = exports.Nav = function Nav(props) {
  return _react2.default.createElement(
    "div",
    { className: "navbar nav-component" },
    _react2.default.createElement(
      "div",
      { className: "row" },
      _react2.default.createElement(PageTitle, null),
      _react2.default.createElement(Icon, {
        icon: "gitlab",
        link: "https://gitlab.com/cordite/cordite",
        toggleModal: function toggleModal(e) {
          return console.log('click');
        } }),
      _react2.default.createElement(Icon, {
        icon: sessionStorage['corditeAccessToken'] ? "sign-out" : "sign-in",
        link: "#",
        toggleModal: props.toggleModal })
    )
  );
}; /*
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


var PageTitle = function PageTitle(props) {
  return _react2.default.createElement(
    "div",
    { className: "page-title-component column column-80 col-site-title" },
    _react2.default.createElement(
      "a",
      { href: "#", className: "site-title float-left" },
      _react2.default.createElement("img", { src: "png/logo-watermark.png", alt: "logo" })
    )
  );
};

var Search = function (_React$Component) {
  (0, _inherits3.default)(Search, _React$Component);

  function Search(props) {
    (0, _classCallCheck3.default)(this, Search);

    var _this = (0, _possibleConstructorReturn3.default)(this, (Search.__proto__ || (0, _getPrototypeOf2.default)(Search)).call(this, props));

    _this.state = {
      value: ""
    };

    _this.handleChange = _this.handleChange.bind(_this);
    return _this;
  }

  (0, _createClass3.default)(Search, [{
    key: "handleChange",
    value: function handleChange(e) {
      this.setState({ value: e.target.value });
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      return _react2.default.createElement(
        "div",
        { className: "search-component column column-40 col-search" },
        _react2.default.createElement("a", {
          href: "#",
          className: "search-btn fa fa-search" }),
        _react2.default.createElement("input", {
          type: "text",
          name: "",
          value: this.state.value,
          onChange: function onChange(e) {
            return _this2.handleChange(e);
          },
          placeholder: "Search..." })
      );
    }
  }]);
  return Search;
}(_react2.default.Component);

var User = function User(props) {
  return _react2.default.createElement(
    "div",
    { className: "column column-30" },
    _react2.default.createElement(
      "div",
      { className: "user-section" },
      _react2.default.createElement(
        "a",
        { href: "#" },
        _react2.default.createElement("img", { src: "http://via.placeholder.com/50x50", alt: "profile photo", className: "circle float-left profile-photo", width: "50", height: "auto" }),
        _react2.default.createElement(
          "div",
          { className: "username" },
          _react2.default.createElement(
            "h4",
            null,
            "Jane Donovan"
          ),
          _react2.default.createElement(
            "p",
            null,
            "Administrator"
          )
        )
      )
    )
  );
};

var SvgLinks = function SvgLinks(props) {
  return _react2.default.createElement(
    "div",
    { className: "svg-icon-component column column-10" },
    _react2.default.createElement(
      "svg",
      {
        xmlns: "http://www.w3.org/2000/svg",
        viewBox: "0 0 305.5 305.7",
        id: "Layer_1" },
      _react2.default.createElement("circle", {
        cx: "152.8",
        cy: "152.8",
        r: "142" }),
      _react2.default.createElement("path", {
        d: "M305.5 153c.9 83.3-69.5 153.1-153.5 152.7C70.5 305.3-.5 237.4 0 151.8.5 69.2 69.4-.6 154.2 0c82.4.6 152.2 69.4 151.3 153zm-69.4-.1c-11.6 7.1-15.5 18.1-16.5 30.2-.8 9.1-.8 18.3-1.1 27.4-.3 10.5-2.6 13.3-12.9 13.1-5.2-.1-6.4 1.6-6.3 6.6.3 14.4.1 14.4 14.6 12.9 14-1.4 21.2-7.6 24.3-21.4 1.1-5 1.5-10.1 1.8-15.3.5-8.9.4-17.9 1.2-26.9.9-10.4 5.7-14.7 16-15.4 2.8-.2 3.9-1.2 3.8-3.9-.1-4.5-.2-9 0-13.5.2-3.8-1.1-5.3-5.1-5.3-7.5 0-11.5-2.9-13.5-10.1-1.3-4.6-2-9.4-2.3-14.2-.6-9.3-.4-18.6-1.1-27.9-1.5-18-11.1-26.7-29.2-26.8-10.3-.1-10.3-.1-10.3 10.6 0 8.6 0 8.6 8.7 8.9 6.3.2 8.7 1.7 9.6 7.8.9 6.5.5 13.3.9 19.9 1 16.2.9 32.8 17.4 43.3zm-166.4-.1c11.3-7.4 15.7-17.8 16.4-30 .6-9.1.4-18.3 1.1-27.4.8-10.2 1.8-14 13.2-13.3 5.3.3 6.4-2 5.7-6.4-.1-.7 0-1.3 0-2 0-11.5 0-11.5-11.8-11.2-16.8.4-27 10.2-27.9 26.9-.5 10.1-.7 20.3-1 30.4-.4 14.5-4.1 19-17.9 21.9-1.2.2-2.7 2.3-2.8 3.7-.4 4.5 0 9-.2 13.5-.2 3.9 1.1 5.3 5.1 5.3 7.1 0 11.6 3.2 13.5 9.6 1.2 4.2 2 8.7 2.2 13.2.6 9.5.6 18.9 1.1 28.4 1.2 22 15.8 30.6 39.6 26.8v-9.4c0-8.8 0-8.8-8.9-9.2-6.2-.3-8.4-1.8-9.1-8.1-.8-7.9-.5-15.9-1.1-23.9-.8-14.8-2.5-29.4-17.2-38.8zm40-12.3c-7.1 0-12.8 5.6-12.7 12.5.1 6.8 5.6 12.1 12.5 12.1 7.3 0 12.7-5.4 12.7-12.4-.2-6.7-5.7-12.2-12.5-12.2zm43.1 24.6c7.3 0 12.3-4.8 12.3-12 .1-7.5-4.9-12.6-12.2-12.7-7.3 0-12.5 5.3-12.4 12.6.1 7.1 5.1 12.1 12.3 12.1zm43.2 0c6.9 0 12.9-5.6 12.9-12.2s-5.9-12.4-12.8-12.4-12.7 5.8-12.6 12.5c.1 6.6 5.8 12.1 12.5 12.1z"
      })
    )
  );
};

var Icon = function Icon(props) {
  return _react2.default.createElement(
    "div",
    { className: "icon-component column column-10" },
    _react2.default.createElement(
      "a",
      {
        href: "javascript:void(0)",
        "data-link": props.icon,
        onClick: function onClick(e) {
          return props.toggleModal(e);
        } },
      _react2.default.createElement("em", { className: "fa fa-" + props.icon })
    )
  );
};
});

;require.register("components/Sidebar/Sidebar.js", function(exports, require, module) {
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Sidebar = undefined;

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Sidebar = exports.Sidebar = function Sidebar(props) {
  return _react2.default.createElement(
    "div",
    { id: "sidebar", className: "column sidebar-component" },
    _react2.default.createElement(
      "ul",
      null,
      props.navOptions[0].map(function (option, index) {
        return _react2.default.createElement(
          "li",
          { key: index },
          _react2.default.createElement(NavLink, { title: option.title, icon: option.icon, handleBtn: props.handleBtn })
        );
      }, { props: props })
    ),
    _react2.default.createElement(
      "h5",
      { className: "title-override" },
      "APIs"
    ),
    _react2.default.createElement(
      "ul",
      null,
      props.navOptions[1].map(function (option, index) {
        return _react2.default.createElement(
          "li",
          { key: index },
          _react2.default.createElement(NavLink, { title: option.title, icon: option.icon, handleBtn: this.props.handleBtn })
        );
      }, { props: props }),
      _react2.default.createElement(
        "li",
        { key: Math.floor(Math.random() * 100) + 7 },
        _react2.default.createElement(
          "button",
          {
            className: "sidebar-button-component",
            "data-btn": "swagger",
            onClick: function onClick(e) {
              return props.handleBtn(e);
            } },
          _react2.default.createElement(
            "svg",
            {
              xmlns: "http://www.w3.org/2000/svg",
              viewBox: "0 0 305.5 305.7",
              id: "Layer_1" },
            _react2.default.createElement("circle", {
              cx: "152.8",
              cy: "152.8",
              r: "142" }),
            _react2.default.createElement("path", {
              d: "M305.5 153c.9 83.3-69.5 153.1-153.5 152.7C70.5 305.3-.5 237.4 0 151.8.5 69.2 69.4-.6 154.2 0c82.4.6 152.2 69.4 151.3 153zm-69.4-.1c-11.6 7.1-15.5 18.1-16.5 30.2-.8 9.1-.8 18.3-1.1 27.4-.3 10.5-2.6 13.3-12.9 13.1-5.2-.1-6.4 1.6-6.3 6.6.3 14.4.1 14.4 14.6 12.9 14-1.4 21.2-7.6 24.3-21.4 1.1-5 1.5-10.1 1.8-15.3.5-8.9.4-17.9 1.2-26.9.9-10.4 5.7-14.7 16-15.4 2.8-.2 3.9-1.2 3.8-3.9-.1-4.5-.2-9 0-13.5.2-3.8-1.1-5.3-5.1-5.3-7.5 0-11.5-2.9-13.5-10.1-1.3-4.6-2-9.4-2.3-14.2-.6-9.3-.4-18.6-1.1-27.9-1.5-18-11.1-26.7-29.2-26.8-10.3-.1-10.3-.1-10.3 10.6 0 8.6 0 8.6 8.7 8.9 6.3.2 8.7 1.7 9.6 7.8.9 6.5.5 13.3.9 19.9 1 16.2.9 32.8 17.4 43.3zm-166.4-.1c11.3-7.4 15.7-17.8 16.4-30 .6-9.1.4-18.3 1.1-27.4.8-10.2 1.8-14 13.2-13.3 5.3.3 6.4-2 5.7-6.4-.1-.7 0-1.3 0-2 0-11.5 0-11.5-11.8-11.2-16.8.4-27 10.2-27.9 26.9-.5 10.1-.7 20.3-1 30.4-.4 14.5-4.1 19-17.9 21.9-1.2.2-2.7 2.3-2.8 3.7-.4 4.5 0 9-.2 13.5-.2 3.9 1.1 5.3 5.1 5.3 7.1 0 11.6 3.2 13.5 9.6 1.2 4.2 2 8.7 2.2 13.2.6 9.5.6 18.9 1.1 28.4 1.2 22 15.8 30.6 39.6 26.8v-9.4c0-8.8 0-8.8-8.9-9.2-6.2-.3-8.4-1.8-9.1-8.1-.8-7.9-.5-15.9-1.1-23.9-.8-14.8-2.5-29.4-17.2-38.8zm40-12.3c-7.1 0-12.8 5.6-12.7 12.5.1 6.8 5.6 12.1 12.5 12.1 7.3 0 12.7-5.4 12.7-12.4-.2-6.7-5.7-12.2-12.5-12.2zm43.1 24.6c7.3 0 12.3-4.8 12.3-12 .1-7.5-4.9-12.6-12.2-12.7-7.3 0-12.5 5.3-12.4 12.6.1 7.1 5.1 12.1 12.3 12.1zm43.2 0c6.9 0 12.9-5.6 12.9-12.2s-5.9-12.4-12.8-12.4-12.7 5.8-12.6 12.5c.1 6.6 5.8 12.1 12.5 12.1z"
            })
          ),
          "SWAGGER"
        )
      )
    ),
    _react2.default.createElement("hr", null),
    _react2.default.createElement(
      "form",
      { action: "https://groups.io/g/cordite-nms/signup?u=4408289288444521305", method: "post", target: "_blank" },
      _react2.default.createElement(
        "div",
        null,
        _react2.default.createElement(
          "h6",
          null,
          "Subscribe to NMS discussion"
        ),
        _react2.default.createElement("br", null),
        _react2.default.createElement("input", { id: "email", type: "email", name: "email", placeholder: "email address", required: "true" }),
        _react2.default.createElement(
          "div",
          { style: { position: "absolute", left: "-5000px" }, "aria-hidden": "true" },
          _react2.default.createElement("input", { type: "text", name: "b_4408289288444521305", tabIndex: "-1", value: "" })
        ),
        _react2.default.createElement(
          "button",
          { className: "sidebar-button-component", id: "groupsio-embedded-subscribe", type: "submit", value: "Subscribe", name: "subscribe" },
          _react2.default.createElement(
            "svg",
            {
              xmlns: "http://www.w3.org/2000/svg",
              viewBox: "0 0 493.497 493.497",
              id: "Layer_1" },
            _react2.default.createElement("circle", {
              cx: "152.8",
              cy: "152.8",
              r: "142" }),
            _react2.default.createElement("path", { d: "M444.556,85.218H48.942C21.954,85.218,0,107.171,0,134.16v225.177c0,26.988,21.954,48.942,48.942,48.942h395.613  c26.988,0,48.941-21.954,48.941-48.942V134.16C493.497,107.171,471.544,85.218,444.556,85.218z M460.87,134.16v225.177  c0,2.574-0.725,4.924-1.793,7.09L343.74,251.081l117.097-117.097C460.837,134.049,460.87,134.096,460.87,134.16z M32.628,359.336  V134.16c0-0.064,0.033-0.11,0.033-0.175l117.097,117.097L34.413,366.426C33.353,364.26,32.628,361.911,32.628,359.336z   M251.784,296.902c-2.692,2.691-7.378,2.691-10.07,0L62.667,117.846h368.172L251.784,296.902z M172.827,274.152l45.818,45.819  c7.512,7.511,17.493,11.645,28.104,11.645c10.61,0,20.592-4.134,28.104-11.645l45.82-45.819l101.49,101.499H71.327L172.827,274.152z  " })
          ),
          " Submit"
        )
      )
    ),
    _react2.default.createElement("hr", null),
    _react2.default.createElement(
      "ul",
      null,
      _react2.default.createElement(
        "li",
        null,
        _react2.default.createElement(
          "span",
          { className: "sidebar-property" },
          "version: ",
          props.buildProperties["nms.version"]
        )
      ),
      _react2.default.createElement(
        "li",
        null,
        _react2.default.createElement(
          "span",
          { className: "sidebar-property" },
          "branch: ",
          props.buildProperties["scmBranch"]
        )
      ),
      _react2.default.createElement(
        "li",
        null,
        _react2.default.createElement(
          "span",
          { className: "sidebar-property" },
          "commit: ",
          props.buildProperties["buildNumber"]
        )
      ),
      _react2.default.createElement(
        "li",
        null,
        _react2.default.createElement(
          "span",
          { className: "sidebar-property" },
          "timestamp: ",
          props.buildProperties["timestamp"]
        )
      )
    )
  );
}; /*
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

var NavLink = function NavLink(props) {
  return _react2.default.createElement(
    "button",
    { className: "sidebar-button-component", "data-btn": props.title, onClick: function onClick(e) {
        return props.handleBtn(e);
      } },
    _react2.default.createElement("em", { className: "fa " + props.icon }),
    props.title.toUpperCase()
  );
};
});

;require.register("components/Spinner/Spinner.js", function(exports, require, module) {
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Spinner = undefined;

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Spinner = exports.Spinner = function Spinner() {
  return _react2.default.createElement(
    "div",
    { className: "spinner-component" },
    _react2.default.createElement("div", { className: "double-bounce1" }),
    _react2.default.createElement("div", { className: "double-bounce2" })
  );
}; /*
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
});

;require.register("components/Table/Table.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Table = undefined;

var _from = require('babel-runtime/core-js/array/from');

var _from2 = _interopRequireDefault(_from);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var Table = exports.Table = function Table(props) {
  var headersList = props.headersList,
      rowData = props.rowData,
      sortTable = props.sortTable,
      admin = props.admin,
      toggleModal = props.toggleModal;

  return _react2.default.createElement(
    'div',
    { className: 'row grid-responsive mt-2' },
    _react2.default.createElement(
      'div',
      { className: 'column ' },
      _react2.default.createElement(
        'div',
        { className: 'card' },
        _react2.default.createElement(
          'div',
          { className: 'card-block' },
          _react2.default.createElement(
            'div',
            { className: 'table-component' },
            _react2.default.createElement(
              'table',
              null,
              _react2.default.createElement(TableHead, {
                headersList: headersList,
                sortTable: sortTable,
                admin: admin
              }),
              _react2.default.createElement(TableBody, {
                headersList: headersList,
                rowData: rowData,
                toggleModal: toggleModal,
                admin: admin
              })
            )
          )
        )
      )
    )
  );
};

var TableHead = function TableHead(props) {
  var headersList = props.headersList,
      sortTable = props.sortTable,
      admin = props.admin;

  var sortCol = function sortCol(e) {
    sortTable(e);
  };
  return _react2.default.createElement(
    'thead',
    null,
    _react2.default.createElement(
      'tr',
      null,
      _react2.default.createElement(
        'th',
        null,
        'Node'
      ),
      (0, _from2.default)(headersList.values()).map(function (h, index) {
        return _react2.default.createElement(
          'th',
          { key: index, 'data-header': h, onClick: function onClick(e) {
              return sortCol(e);
            } },
          h,
          _react2.default.createElement('img', { src: 'png/sort.png', 'data-header': h })
        );
      }),
      admin ? _react2.default.createElement(
        'th',
        null,
        'Controls'
      ) : null
    )
  );
};

var TableBody = function TableBody(props) {
  var headersList = props.headersList,
      rowData = props.rowData,
      toggleModal = props.toggleModal,
      admin = props.admin;

  var tr = rowData.map(function (node, index) {
    return _react2.default.createElement(TableRow, {
      key: index,
      node: node,
      headersList: headersList,
      toggleModal: toggleModal,
      admin: admin });
  });
  return _react2.default.createElement(
    'tbody',
    null,
    tr
  );
};

var TableRow = function TableRow(props) {
  var node = props.node,
      headersList = props.headersList,
      toggleModal = props.toggleModal,
      admin = props.admin;

  var valueArray = [];

  headersList.forEach(function (value, key) {
    if (node.hasOwnProperty(key)) {
      valueArray.push(node[key]);
    } else {
      valueArray.push("");
    }
  });

  return _react2.default.createElement(
    'tr',
    { className: 'table-row-component' },
    _react2.default.createElement(
      'td',
      null,
      _react2.default.createElement(TableTooltip, { tooltip: node.nodeKey }),
      node.nodeKey.slice(0, 3) + '...' + node.nodeKey.substr(-3)
    ),
    valueArray.map(function (value, index) {
      var td = void 0;
      typeof value === "boolean" ? td = _react2.default.createElement('td', { className: value.toString(), key: index }) : td = _react2.default.createElement(
        'td',
        { key: index },
        value.toString()
      );
      return td;
    }),
    admin ? _react2.default.createElement(
      'td',
      { key: 9 },
      _react2.default.createElement(
        'button',
        {
          className: 'wibble' },
        _react2.default.createElement('em', { className: 'fa fa-trash', 'data-link': 'delete', onClick: function onClick(e) {
            toggleModal(e, node);
          } })
      )
    ) : null
  );
};

var TableTooltip = function TableTooltip(props) {
  return _react2.default.createElement(
    'div',
    { className: 'table-tooltip-component' },
    props.tooltip
  );
};

Table.propTypes = {
  headersList: _propTypes2.default.object.isRequired,
  rowData: _propTypes2.default.array.isRequired
};

TableHead.propTypes = {
  headersList: _propTypes2.default.object.isRequired
};

TableBody.propTypes = {
  rowData: _propTypes2.default.array.isRequired
};

TableRow.propTypes = {
  nodes: _propTypes2.default.object
};
});

;require.register("containers/App/App.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _getPrototypeOf = require('babel-runtime/core-js/object/get-prototype-of');

var _getPrototypeOf2 = _interopRequireDefault(_getPrototypeOf);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Default = require('containers/Default/Default');

var _Default2 = _interopRequireDefault(_Default);

var _Login = require('containers/Login/Login');

var _restCalls = require('scripts/restCalls');

var _Modal = require('components/Modal/Modal');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var App = function (_React$Component) {
  (0, _inherits3.default)(App, _React$Component);

  function App(props) {
    (0, _classCallCheck3.default)(this, App);

    var _this = (0, _possibleConstructorReturn3.default)(this, (App.__proto__ || (0, _getPrototypeOf2.default)(App)).call(this, props));

    _this.state = {
      admin: sessionStorage["corditeAccessToken"] ? true : false,
      status: 'done',
      subDomain: 'default',
      style: false,
      modal: ''
    };

    _this.NMSLogin = _this.NMSLogin.bind(_this);
    _this.toggleModal = _this.toggleModal.bind(_this);
    _this.setAdmin = _this.setAdmin.bind(_this);
    _this.deleteNode = _this.deleteNode.bind(_this);
    return _this;
  }

  (0, _createClass3.default)(App, [{
    key: 'NMSLogin',
    value: function NMSLogin(loginData) {
      return (0, _restCalls.login)(loginData).then(function () {
        return (0, _restCalls.checkAuth)();
      }).then(function (status) {
        if (status != 200) return "fail";
        return "success";
      }).catch(function (err) {
        return console.log(err);
      });
    }
  }, {
    key: 'toggleModal',
    value: function toggleModal(e, node) {
      if (!e) {
        this.setState({
          modal: '',
          style: !this.state.style
        });
      } else if (e.target.dataset.link) {
        this.setState({
          modal: e.target.dataset.link || '',
          style: !this.state.style,
          selectedNode: !!node ? node : null
        });
      }
    }
  }, {
    key: 'setAdmin',
    value: function setAdmin(adminFlag) {
      this.setState({ admin: adminFlag });
    }
  }, {
    key: 'deleteNode',
    value: function deleteNode() {
      var _this2 = this;

      (0, _restCalls.deleteNodes)(this.state.selectedNode.nodeKey).then(function (result) {
        return _this2.setState({ selectedNode: null });
      });
    }
  }, {
    key: 'render',
    value: function render() {
      var page = null;
      switch (this.state.subDomain) {
        case 'login':
          page = _react2.default.createElement(_Login.Login, { nmsLogin: this.NMSLogin });
          break;
        case 'default':
          page = _react2.default.createElement(_Default2.default, {
            toggleModal: this.toggleModal,
            admin: this.state.admin });
          break;
        default:
          page = _react2.default.createElement(_Login.Login, { nmsLogin: this.nmsLogin });
          break;
      }

      var modal = null;
      switch (this.state.modal) {
        case 'sign-in':
          modal = _react2.default.createElement(_Modal.LoginModal, {
            toggleModal: this.toggleModal,
            style: this.state.style,
            nmsLogin: this.NMSLogin,
            setAdmin: this.setAdmin });
          break;
        case 'sign-out':
          modal = _react2.default.createElement(_Modal.LogoutModal, {
            toggleModal: this.toggleModal,
            style: this.state.style,
            setAdmin: this.setAdmin });
          break;
        case 'delete':
          modal = _react2.default.createElement(_Modal.DeleteModal, {
            toggleModal: this.toggleModal,
            style: this.state.style,
            setAdmin: this.setAdmin,
            selectedNode: this.state.selectedNode,
            deleteNode: this.deleteNode });
          break;
        default:
          modal = "";
          break;
      }

      return _react2.default.createElement(
        'div',
        { className: 'app-component' },
        this.state.status == 'done' ? page : "",
        modal
      );
    }
  }]);
  return App;
}(_react2.default.Component); /*
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


exports.default = App;
});

;require.register("containers/Default/Default.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _regenerator = require('babel-runtime/regenerator');

var _regenerator2 = _interopRequireDefault(_regenerator);

var _asyncToGenerator2 = require('babel-runtime/helpers/asyncToGenerator');

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var _getPrototypeOf = require('babel-runtime/core-js/object/get-prototype-of');

var _getPrototypeOf2 = _interopRequireDefault(_getPrototypeOf);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Page = require('containers/Page/Page');

var _Nav = require('components/Nav/Nav');

var _Sidebar = require('components/Sidebar/Sidebar');

var _restCalls = require('scripts/restCalls');

var _processData = require('scripts/processData');

var _headersList = require('scripts/headersList');

var _navOptions = require('navOptions.json');

var _navOptions2 = _interopRequireDefault(_navOptions);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var Default = function (_React$Component) {
  (0, _inherits3.default)(Default, _React$Component);

  function Default(props) {
    (0, _classCallCheck3.default)(this, Default);

    var _this = (0, _possibleConstructorReturn3.default)(this, (Default.__proto__ || (0, _getPrototypeOf2.default)(Default)).call(this, props));

    _this.getData = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee() {
      var notaries, nodes, properties;
      return _regenerator2.default.wrap(function _callee$(_context) {
        while (1) {
          switch (_context.prev = _context.next) {
            case 0:
              _context.next = 2;
              return (0, _restCalls.getNotaries)();

            case 2:
              notaries = _context.sent;
              _context.next = 5;
              return (0, _restCalls.getNodes)();

            case 5:
              nodes = _context.sent;
              _context.next = 8;
              return (0, _restCalls.getBuildProperties)();

            case 8:
              properties = _context.sent;
              _context.next = 11;
              return (0, _processData.mutateNodes)(nodes);

            case 11:
              nodes = _context.sent;

              nodes = (0, _processData.isNotary)(nodes, notaries);
              nodes = (0, _processData.sortNodes)('Organisational Unit', nodes, _headersList.headersList);

              this.setState({
                nodes: nodes,
                notaries: notaries,
                buildProperties: properties
              });

            case 15:
            case 'end':
              return _context.stop();
          }
        }
      }, _callee, this);
    }));

    _this.handleBtn = function (event, data) {
      var btnType = event.target.dataset.btn;

      switch (btnType.toLowerCase()) {
        case 'swagger':
          window.location = document.baseURI + "swagger/";
          break;
        case 'dashboard':
          _this.setState({ page: 'home' });
          break;
        case 'braid':
          // window.location = "/braid/api/"
          (0, _restCalls.getBraidAPI)().then(function (result) {
            _this.setState({
              braid: result,
              page: 'braid'
            });
          });

          break;
        default:
          break;
      }
    };

    _this.state = {
      nodes: [],
      notaries: [],
      page: 'home',
      braid: {},
      buildProperties: {}
    };

    _this.getData = _this.getData.bind(_this);
    _this.sortTable = _this.sortTable.bind(_this);
    return _this;
  }

  (0, _createClass3.default)(Default, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      this.getData();
    }
  }, {
    key: 'sortTable',
    value: function sortTable(e) {
      var sortedNodes = (0, _processData.sortNodes)(e.target.dataset.header, this.state.nodes, _headersList.headersList);
      this.setState({ nodes: sortedNodes });
    }
  }, {
    key: 'render',
    value: function render() {
      return _react2.default.createElement(
        'div',
        { className: 'default-component' },
        _react2.default.createElement(_Nav.Nav, { toggleModal: this.props.toggleModal }),
        _react2.default.createElement(
          'div',
          { className: 'row' },
          this.props.admin ? _react2.default.createElement(_Sidebar.Sidebar, {
            navOptions: _navOptions2.default,
            buildProperties: this.state.buildProperties,
            handleBtn: this.handleBtn }) : "",
          _react2.default.createElement(
            'section',
            { id: 'main-content', className: "column" + (this.props.admin ? " column-offset-20" : "") },
            _react2.default.createElement(_Page.Page, {
              headersList: _headersList.headersList,
              nodes: this.state.nodes,
              notaries: this.state.notaries,
              page: this.state.page,
              sortTable: this.sortTable,
              json: this.state.braid,
              toggleModal: this.props.toggleModal,
              admin: this.props.admin,
              getData: this.getData
            })
          )
        )
      );
    }
  }]);
  return Default;
}(_react2.default.Component);

exports.default = Default;
});

;require.register("containers/Login/Login.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.LoginContainer = exports.Login = undefined;

var _regenerator = require('babel-runtime/regenerator');

var _regenerator2 = _interopRequireDefault(_regenerator);

var _asyncToGenerator2 = require('babel-runtime/helpers/asyncToGenerator');

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var _getPrototypeOf = require('babel-runtime/core-js/object/get-prototype-of');

var _getPrototypeOf2 = _interopRequireDefault(_getPrototypeOf);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var handleLogin = function handleLogin(nmsLogin, loginData) {
  var result = nmsLogin(loginData);
  result == 'fail' ? true : false;
}; /*
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
var Login = exports.Login = function Login(props) {
  return _react2.default.createElement(
    'div',
    { className: 'login-component' },
    _react2.default.createElement(LoginContainer, props)
  );
};

var LoginContainer = exports.LoginContainer = function LoginContainer(props) {
  return _react2.default.createElement(
    'div',
    { className: 'login-container-component' },
    _react2.default.createElement(LoginLogo, { title: 'Cordite stats' }),
    _react2.default.createElement(LoginMain, {
      nmsLogin: props.nmsLogin,
      toggleModal: props.toggleModal,
      setAdmin: props.setAdmin }),
    _react2.default.createElement(LoginFooter, null)
  );
};

var LoginLogo = function LoginLogo(props) {
  return _react2.default.createElement('div', { className: 'login-logo-component' });
};

var LoginMain = function LoginMain(props) {
  return _react2.default.createElement(
    'div',
    { className: 'login-main-component' },
    _react2.default.createElement(LoginTitle, { title: 'Welcome, Please login' }),
    _react2.default.createElement(LoginForm, {
      nmsLogin: props.nmsLogin,
      toggleModal: props.toggleModal,
      setAdmin: props.setAdmin })
  );
};

var LoginFooter = function LoginFooter() {
  return _react2.default.createElement(
    'div',
    { className: 'login-footer-component' },
    _react2.default.createElement(
      'div',
      { className: 'brand-copyright' },
      '\xA9 2018 Cordite'
    ),
    _react2.default.createElement(
      'div',
      { className: 'footer-links' },
      _react2.default.createElement(
        'a',
        { href: '#' },
        'About'
      ),
      '\xA0|\xA0',
      _react2.default.createElement(
        'a',
        { href: '#' },
        'Privacy'
      ),
      '\xA0|\xA0',
      _react2.default.createElement(
        'a',
        { href: '#' },
        'Contact Us'
      )
    )
  );
};

var LoginTitle = function LoginTitle(props) {
  return _react2.default.createElement(
    'div',
    { className: 'login-title-component' },
    props.title
  );
};

var LoginForm = function (_React$Component) {
  (0, _inherits3.default)(LoginForm, _React$Component);

  function LoginForm(props) {
    (0, _classCallCheck3.default)(this, LoginForm);

    var _this = (0, _possibleConstructorReturn3.default)(this, (LoginForm.__proto__ || (0, _getPrototypeOf2.default)(LoginForm)).call(this, props));

    _this.handleClick = function () {
      var _ref = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee(e) {
        var loginData, result;
        return _regenerator2.default.wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                e.preventDefault();

                if (!(e.target.dataset.btn == 'cancel')) {
                  _context.next = 4;
                  break;
                }

                this.props.toggleModal();
                return _context.abrupt('return');

              case 4:
                loginData = {
                  user: this.state.user,
                  password: this.state.password
                };
                _context.next = 7;
                return this.props.nmsLogin(loginData);

              case 7:
                result = _context.sent;

                if (result == 'fail') {
                  this.setState({ error: 'error' });
                } else {
                  this.props.setAdmin(true);
                  this.props.toggleModal();
                }

              case 9:
              case 'end':
                return _context.stop();
            }
          }
        }, _callee, this);
      }));

      return function (_x) {
        return _ref.apply(this, arguments);
      };
    }();

    _this.state = {
      user: '',
      password: '',
      error: ''
    };
    _this.handleChange = _this.handleChange.bind(_this);
    _this.handleClick = _this.handleClick.bind(_this);
    return _this;
  }

  (0, _createClass3.default)(LoginForm, [{
    key: 'handleChange',
    value: function handleChange(e) {
      e.preventDefault();
      var _e$target = e.target,
          type = _e$target.type,
          value = _e$target.value;

      switch (type) {
        case "text":
          this.setState({ user: value, error: '' });
          break;
        case "password":
          this.setState({ password: value, error: '' });
          break;
        default:
          break;
      }
    }
  }, {
    key: 'render',
    value: function render() {
      return _react2.default.createElement(
        'form',
        { className: 'login-form-component' },
        _react2.default.createElement(FormTextInput, {
          placeholder: 'Username',
          value: this.state.value,
          handleChange: this.handleChange }),
        _react2.default.createElement(FormPassword, {
          placeholder: 'Password',
          value: this.state.value,
          handleChange: this.handleChange }),
        _react2.default.createElement(FormError, { error: this.state.error }),
        _react2.default.createElement(FormButtons, { onClick: this.handleClick })
      );
    }
  }]);
  return LoginForm;
}(_react2.default.Component);

var FormTextInput = function FormTextInput(props) {
  var placeholder = props.placeholder,
      value = props.value,
      handleChange = props.handleChange;

  return _react2.default.createElement(
    'div',
    { className: 'form-text-input-component' },
    _react2.default.createElement('input', {
      autoFocus: 'true',
      className: 'form-control',
      placeholder: placeholder,
      value: value,
      type: 'text',
      onChange: function onChange(e) {
        return handleChange(e);
      } })
  );
};

var FormPassword = function FormPassword(props) {
  var placeholder = props.placeholder,
      value = props.value,
      handleChange = props.handleChange;

  return _react2.default.createElement(
    'div',
    { className: 'form-password-component' },
    _react2.default.createElement('input', {
      className: 'form-control',
      placeholder: placeholder,
      value: value,
      type: 'password',
      onChange: function onChange(e) {
        return handleChange(e);
      } })
  );
};

var FormButtons = function FormButtons(props) {
  var _onClick = props.onClick;

  return _react2.default.createElement(
    'div',
    { className: 'form-buttons-component' },
    _react2.default.createElement(
      'a',
      { href: '#', className: 'btn' },
      'Forgot your password?'
    ),
    _react2.default.createElement(
      'button',
      {
        className: 'btn',
        onClick: function onClick(e) {
          return _onClick(e);
        } },
      'Log In'
    ),
    _react2.default.createElement(
      'button',
      {
        className: 'btn',
        'data-btn': 'cancel',
        onClick: function onClick(e) {
          return _onClick(e);
        } },
      'Cancel'
    )
  );
};

var FormError = function FormError(props) {
  return _react2.default.createElement(
    'div',
    { className: 'form-error-component ' + props.error },
    'Username/password is invalid'
  );
};
});

;require.register("containers/Page/Page.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Page = undefined;

var _keys = require('babel-runtime/core-js/object/keys');

var _keys2 = _interopRequireDefault(_keys);

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Home = require('containers/Pages/Home/Home');

var _Swagger = require('containers/Pages/Swagger/Swagger');

var _Braid = require('containers/Pages/Braid/Braid');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var PAGES = {
  home: _Home.Home,
  swagger: _Swagger.Swagger,
  braid: _Braid.Braid
}; /*
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
var Page = exports.Page = function Page(props) {
  var Handler = PAGES[props.page];

  return _react2.default.createElement(Handler, props);
};

Page.propTypes = {
  page: _propTypes2.default.oneOf((0, _keys2.default)(PAGES)).isRequired
};
});

;require.register("containers/Pages/Braid/Braid.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Braid = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _DisplayBraid = require('components/DisplayBraid/DisplayBraid');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Braid = exports.Braid = function Braid(props) {
  var json = props.json;

  return _react2.default.createElement(
    'section',
    { className: 'braid-component' },
    _react2.default.createElement(_DisplayBraid.DisplayBraid, { json: json })
  );
}; /*
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


Braid.propTypes = {};
});

;require.register("containers/Pages/Home/Home.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Home = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Nav = require('components/Nav/Nav');

var _Table = require('components/Table/Table');

var _MyMap = require('components/Map/MyMap');

var _MyMap2 = _interopRequireDefault(_MyMap);

var _Metrics = require('components/Metrics/Metrics');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var Home = exports.Home = function Home(props) {
  var nodes = props.nodes,
      notaries = props.notaries,
      headersList = props.headersList,
      sortTable = props.sortTable;

  return _react2.default.createElement(
    'section',
    { className: 'home-component' },
    _react2.default.createElement(_MyMap2.default, { nodes: nodes }),
    _react2.default.createElement(_Metrics.Metrics, {
      nodes: nodes,
      notaries: notaries
    }),
    _react2.default.createElement(_Table.Table, {
      headersList: headersList,
      rowData: nodes,
      sortTable: sortTable,
      toggleModal: props.toggleModal,
      admin: props.admin
    })
  );
};

Home.propTypes = {
  headersList: _propTypes2.default.object.isRequired,
  nodes: _propTypes2.default.array.isRequired,
  notaries: _propTypes2.default.array.isRequired

  /*
  <DemoMap nodes={nodes}/>
  <Metrics 
    nodes={nodes}
    notaries={notaries}
  />
  
  */

};
});

;require.register("containers/Pages/Swagger/Swagger.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Swagger = undefined;

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Swagger = exports.Swagger = function Swagger(props) {
  console.log(document.baseURI);
  return _react2.default.createElement(
    'div',
    { className: 'swagger-component' },
    _react2.default.createElement('iframe', {
      title: 'Swagger API', src: '{ document.baseURI }swagger/#/admin/post_admin_api_login' })
  );
}; /*
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
});

;require.register("initialize.js", function(exports, require, module) {
'use strict';

require('babel-polyfill');

require('whatwg-fetch');

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactDom = require('react-dom');

var _reactDom2 = _interopRequireDefault(_reactDom);

var _App = require('containers/App/App');

var _App2 = _interopRequireDefault(_App);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var render = function render(Component) {
  _reactDom2.default.render(_react2.default.createElement(Component, null), document.querySelector('#app'));
}; /*
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


document.addEventListener('DOMContentLoaded', function () {
  render(_App2.default);
});
});

require.register("mapStyle.json", function(exports, require, module) {
module.exports = [
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#a1dcd3"
      }
    ]
  },
  {
    "elementType": "labels.icon",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "rgba(0, 0, 0, 0)"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": ""
      },
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#FFFFFF"
      }
    ]
  },
  {
    "featureType": "administrative.country",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#999999"
      },
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.province",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#999999"
      },
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.land_parcel",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#bdbdbd"
      },
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.neighborhood",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.neighborhood",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#757575"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#181818"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#616161"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#1b1b1b"
      }
    ]
  },
  {
    "featureType": "road",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.fill",
    "stylers": [
      {
        "color": "#2c2c2c"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.icon",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#8a8a8a"
      }
    ]
  },
  {
    "featureType": "road.arterial",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#373737"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#3c3c3c"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#4e4e4e"
      }
    ]
  },
  {
    "featureType": "road.local",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#616161"
      }
    ]
  },
  {
    "featureType": "transit",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#757575"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#FFFFFF"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#3d3d3d"
      }
    ]
  }
];
});

require.register("navOptions.json", function(exports, require, module) {
module.exports = [
  [
    {
      "title": "dashboard",
      "icon": "fa-desktop"
    }
  ],
  [
    {
      "title": "Braid",
      "icon": "fa-random"
    }
  ]
];
});

require.register("scripts/geoCode.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.nodesLatLong = exports.lookupLatLong = undefined;

var _map = require('babel-runtime/core-js/map');

var _map2 = _interopRequireDefault(_map);

var _regenerator = require('babel-runtime/regenerator');

var _regenerator2 = _interopRequireDefault(_regenerator);

var _asyncToGenerator2 = require('babel-runtime/helpers/asyncToGenerator');

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var lookupLatLong = exports.lookupLatLong = function () {
  var _ref = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee(location) {
    var params, response, status, locations;
    return _regenerator2.default.wrap(function _callee$(_context) {
      while (1) {
        switch (_context.prev = _context.next) {
          case 0:
            params = {
              address: '' + location.city,
              key: API_KEY,
              region: location.country.toLowerCase()
            };

            url.search = new URLSearchParams(params);

            _context.next = 4;
            return fetch(url);

          case 4:
            response = _context.sent;
            _context.next = 7;
            return response.status;

          case 7:
            status = _context.sent;

            if (!(status === 200)) {
              _context.next = 15;
              break;
            }

            _context.next = 11;
            return response.json();

          case 11:
            locations = _context.sent;
            return _context.abrupt('return', locations.results[0].geometry.location);

          case 15:
            return _context.abrupt('return', response.statusText);

          case 16:
          case 'end':
            return _context.stop();
        }
      }
    }, _callee, this);
  }));

  return function lookupLatLong(_x) {
    return _ref.apply(this, arguments);
  };
}();

var nodesLatLong = exports.nodesLatLong = function () {
  var _ref2 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee2(uniqueLocations) {
    var locMap, i, tempLatLong;
    return _regenerator2.default.wrap(function _callee2$(_context2) {
      while (1) {
        switch (_context2.prev = _context2.next) {
          case 0:
            locMap = new _map2.default();
            _context2.t0 = _regenerator2.default.keys(uniqueLocations);

          case 2:
            if ((_context2.t1 = _context2.t0()).done) {
              _context2.next = 10;
              break;
            }

            i = _context2.t1.value;
            _context2.next = 6;
            return lookupLatLong(uniqueLocations[i]);

          case 6:
            tempLatLong = _context2.sent;

            locMap.set('' + uniqueLocations[i].city + uniqueLocations[i].country, {
              lat: tempLatLong.lat,
              lng: tempLatLong.lng
            });
            _context2.next = 2;
            break;

          case 10:
            return _context2.abrupt('return', locMap);

          case 11:
          case 'end':
            return _context2.stop();
        }
      }
    }, _callee2, this);
  }));

  return function nodesLatLong(_x2) {
    return _ref2.apply(this, arguments);
  };
}();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var API_KEY = 'AIzaSyC4QrPK-xamnJwHo-CFW0XzeDj4INbsQYU';
var url = new URL('https://maps.googleapis.com/maps/api/geocode/json');
});

;require.register("scripts/headersList.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.headersList = undefined;

var _map = require('babel-runtime/core-js/map');

var _map2 = _interopRequireDefault(_map);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

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
var OU = 'OU',
    O = 'O',
    L = 'L',
    C = 'C',
    N = 'N';

var headersList = exports.headersList = new _map2.default([[OU, 'Organisational Unit'], [O, 'Organisation'], [L, 'Location'], [C, 'Country'], [N, 'Notary']]);
});

;require.register("scripts/jwtProcess.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
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
var getJWTExpiryDate = function getJWTExpiryDate(token) {
  var jwtArray = token.split('.');
  var result = JSON.parse(atob(jwtArray[1]));
  return result.exp;
};

var checkToken = exports.checkToken = function checkToken(token) {
  var unixTime = Date.now();
  return Math.floor(unixTime / 1000) <= getJWTExpiryDate(token);
};
});

;require.register("scripts/processData.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.sortNodes = exports.isNotary = exports.geoCodeNodes = exports.getLocations = exports.getHeaders = exports.parseNodes = exports.mutateNodes = undefined;

var _slicedToArray2 = require('babel-runtime/helpers/slicedToArray');

var _slicedToArray3 = _interopRequireDefault(_slicedToArray2);

var _map = require('babel-runtime/core-js/map');

var _map2 = _interopRequireDefault(_map);

var _getIterator2 = require('babel-runtime/core-js/get-iterator');

var _getIterator3 = _interopRequireDefault(_getIterator2);

var _regenerator = require('babel-runtime/regenerator');

var _regenerator2 = _interopRequireDefault(_regenerator);

var _asyncToGenerator2 = require('babel-runtime/helpers/asyncToGenerator');

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var mutateNodes = exports.mutateNodes = function () {
  var _ref = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee(nodes) {
    var formatedNodes, uniqueLocations, uniqueLocationsMap;
    return _regenerator2.default.wrap(function _callee$(_context) {
      while (1) {
        switch (_context.prev = _context.next) {
          case 0:
            formatedNodes = parseNodes(nodes);
            uniqueLocations = getLocations(formatedNodes);
            _context.next = 4;
            return geoCode.nodesLatLong(uniqueLocations);

          case 4:
            uniqueLocationsMap = _context.sent;

            formatedNodes = geoCodeNodes(formatedNodes, uniqueLocationsMap);
            return _context.abrupt('return', formatedNodes);

          case 7:
          case 'end':
            return _context.stop();
        }
      }
    }, _callee, this);
  }));

  return function mutateNodes(_x) {
    return _ref.apply(this, arguments);
  };
}();

var _geoCode = require('scripts/geoCode');

var geoCode = _interopRequireWildcard(_geoCode);

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var sortObject = {
  sortCiteria: ""
}; /*
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
var parseNodes = exports.parseNodes = function parseNodes(nodes) {
  var nodeArray = [];
  var _iteratorNormalCompletion = true;
  var _didIteratorError = false;
  var _iteratorError = undefined;

  try {
    for (var _iterator = (0, _getIterator3.default)(nodes), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
      var i = _step.value;

      nodeArray = parseParties(i, nodeArray);
    }
  } catch (err) {
    _didIteratorError = true;
    _iteratorError = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion && _iterator.return) {
        _iterator.return();
      }
    } finally {
      if (_didIteratorError) {
        throw _iteratorError;
      }
    }
  }

  return nodeArray;
};

function parseParties(node, formatedNodeArray) {
  for (var i in node.parties) {
    formatedNodeArray.push(parseParty(node.nodeKey, node.parties[i]));
  }
  return formatedNodeArray;
}

function parseParty(nodeKey, party) {
  var partyObj = {};
  partyObj = parseX500(party, partyObj);
  partyObj = parseKey(party, partyObj);
  partyObj.nodeKey = nodeKey;
  return partyObj;
}

function parseX500(party, partyObj) {
  var tokens = party.name.split(',');
  for (var i in tokens) {
    tokens[i] = tokens[i].trim();
    var letTokenSplit = tokens[i].split('=');
    partyObj[letTokenSplit[0]] = letTokenSplit[1];
  }
  return partyObj;
}

function parseKey(party, partyObj) {
  partyObj.key = party.key;
  return partyObj;
}

var getHeaders = exports.getHeaders = function getHeaders(list) {
  var headerArray = [];
  console.log(list);
  if (list.size == 0) return headerArray;
  var _iteratorNormalCompletion2 = true;
  var _didIteratorError2 = false;
  var _iteratorError2 = undefined;

  try {
    for (var _iterator2 = (0, _getIterator3.default)(list.values()), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
      var value = _step2.value;

      headerArray.push(value);
    }
  } catch (err) {
    _didIteratorError2 = true;
    _iteratorError2 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion2 && _iterator2.return) {
        _iterator2.return();
      }
    } finally {
      if (_didIteratorError2) {
        throw _iteratorError2;
      }
    }
  }

  return headerArray;
};

var getLocations = exports.getLocations = function getLocations(nodes) {
  var locationArray = [];
  for (var i in nodes) {
    var obj = { city: nodes[i].L, country: nodes[i].C };
    locationArray.push(obj);
  }
  locationArray = uniqueList(locationArray);
  return locationArray;
};

function uniqueList(locationArray) {
  var unique = locationArray.filter(uniqueObjects);
  return unique;
}

function uniqueObjects(value, index, self) {
  for (var i = 0; i < index; i++) {
    if (self[i].city === self[index].city && self[i].country === self[index].country) return false;
  }
  return true;
}

var geoCodeNodes = exports.geoCodeNodes = function geoCodeNodes(nodesArray, locationsMap) {
  var nodes = nodesArray;
  nodes = nodes.map(function (node, index) {
    var keyObj = '' + node.L + node.C;
    node.lat = locationsMap.get(keyObj).lat;
    node.lng = locationsMap.get(keyObj).lng;
    return node;
  });
  return nodes;
};

var isNotary = exports.isNotary = function isNotary(nodes, notaries) {
  var notaryMap = createNotaryMap(notaries);
  for (var node in nodes) {
    if (notaryMap.get(nodes[node].key)) {
      nodes[node].N = true;
    } else {
      nodes[node].N = false;
    }
  }
  return nodes;
};

function createNotaryMap(notaries) {
  var notaryMap = new _map2.default();
  notaries.forEach(function (notary) {
    notaryMap.set(notary.notaryInfo.identity.owningKey, notary.notaryInfo.identity.owningKey);
  });
  return notaryMap;
}

var sortNodes = exports.sortNodes = function sortNodes(sortValue, nodeArray, notaryMap) {
  var sortKey = void 0;
  var _iteratorNormalCompletion3 = true;
  var _didIteratorError3 = false;
  var _iteratorError3 = undefined;

  try {
    for (var _iterator3 = (0, _getIterator3.default)(notaryMap), _step3; !(_iteratorNormalCompletion3 = (_step3 = _iterator3.next()).done); _iteratorNormalCompletion3 = true) {
      var _ref2 = _step3.value;

      var _ref3 = (0, _slicedToArray3.default)(_ref2, 2);

      var key = _ref3[0];
      var value = _ref3[1];

      if (value === sortValue) sortKey = key;
    }
  } catch (err) {
    _didIteratorError3 = true;
    _iteratorError3 = err;
  } finally {
    try {
      if (!_iteratorNormalCompletion3 && _iterator3.return) {
        _iterator3.return();
      }
    } finally {
      if (_didIteratorError3) {
        throw _iteratorError3;
      }
    }
  }

  if (sortKey === sortObject.sortCiteria) return nodeArray.reverse();else sortObject.sortCiteria = sortKey;

  nodeArray.sort(function (a, b) {
    var aValue = getProperty(a, sortKey, "");
    var bValue = getProperty(b, sortKey, "");
    return aValue.localeCompare(bValue);
  });

  return nodeArray;
};

function getProperty(obj, propertyName, defaultValue) {
  if (obj.hasOwnProperty(obj, propertyName) && obj[propertyName]) {
    return obj[propertyName];
  } else {
    return defaultValue;
  }
}

Array.prototype.equals = function (array) {
  if (!array) return false;
  if (this.length != array.length) {
    console.log('in here');return false;
  }

  for (var i = 0; i < this.length; i++) {
    if (this[i]['key'] != array[i]['key']) return false;
  }
  return true;
};

Object.defineProperty(Array.prototype, "equals", { enumerable: false });
});

require.register("scripts/restCalls.js", function(exports, require, module) {
'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.deleteNodes = exports.getBraidAPI = exports.getBuildProperties = exports.getNotaries = exports.getNodes = exports.checkAuth = exports.login = undefined;

var _regenerator = require('babel-runtime/regenerator');

var _regenerator2 = _interopRequireDefault(_regenerator);

var _stringify = require('babel-runtime/core-js/json/stringify');

var _stringify2 = _interopRequireDefault(_stringify);

var _asyncToGenerator2 = require('babel-runtime/helpers/asyncToGenerator');

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var login = exports.login = function () {
  var _ref = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee(loginData) {
    var response, status;
    return _regenerator2.default.wrap(function _callee$(_context) {
      while (1) {
        switch (_context.prev = _context.next) {
          case 0:
            _context.next = 2;
            return fetch(url + '/admin/api/login', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json'
              },
              body: (0, _stringify2.default)(loginData)
            });

          case 2:
            response = _context.sent;
            _context.next = 5;
            return response.status;

          case 5:
            status = _context.sent;

            if (!(status === 200)) {
              _context.next = 12;
              break;
            }

            _context.next = 9;
            return response.text();

          case 9:
            sessionStorage["corditeAccessToken"] = _context.sent;
            _context.next = 13;
            break;

          case 12:
            console.log(response);

          case 13:
            return _context.abrupt('return', response);

          case 14:
          case 'end':
            return _context.stop();
        }
      }
    }, _callee, this);
  }));

  return function login(_x) {
    return _ref.apply(this, arguments);
  };
}();

var checkAuth = exports.checkAuth = function () {
  var _ref2 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee2() {
    var status, token;
    return _regenerator2.default.wrap(function _callee2$(_context2) {
      while (1) {
        switch (_context2.prev = _context2.next) {
          case 0:
            status = 403;
            token = sessionStorage['corditeAccessToken'];

            if (token && (0, _jwtProcess.checkToken)(token)) {
              status = 200;
            }
            return _context2.abrupt('return', status);

          case 4:
          case 'end':
            return _context2.stop();
        }
      }
    }, _callee2, this);
  }));

  return function checkAuth() {
    return _ref2.apply(this, arguments);
  };
}();

var getNodes = exports.getNodes = function () {
  var _ref3 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee3() {
    var token, response, nodes;
    return _regenerator2.default.wrap(function _callee3$(_context3) {
      while (1) {
        switch (_context3.prev = _context3.next) {
          case 0:
            token = sessionStorage["corditeAccessToken"];
            _context3.next = 3;
            return fetch(url + '/admin/api/nodes', {
              method: 'GET',
              headers: {
                'accept': 'application/json',
                "Authorization": 'Bearer ' + token
              }
            });

          case 3:
            response = _context3.sent;
            _context3.next = 6;
            return response.json();

          case 6:
            nodes = _context3.sent;
            return _context3.abrupt('return', nodes);

          case 8:
          case 'end':
            return _context3.stop();
        }
      }
    }, _callee3, this);
  }));

  return function getNodes() {
    return _ref3.apply(this, arguments);
  };
}();

var getNotaries = exports.getNotaries = function () {
  var _ref4 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee4() {
    var response, notaries;
    return _regenerator2.default.wrap(function _callee4$(_context4) {
      while (1) {
        switch (_context4.prev = _context4.next) {
          case 0:
            _context4.next = 2;
            return fetch(url + '/admin/api/notaries', {
              method: 'GET',
              headers: {
                'accept': 'application/json',
                "Authorization": 'Bearer ' + sessionStorage["corditeAccessToken"]
              }
            });

          case 2:
            response = _context4.sent;
            _context4.next = 5;
            return response.json();

          case 5:
            notaries = _context4.sent;
            return _context4.abrupt('return', notaries);

          case 7:
          case 'end':
            return _context4.stop();
        }
      }
    }, _callee4, this);
  }));

  return function getNotaries() {
    return _ref4.apply(this, arguments);
  };
}();

var getBuildProperties = exports.getBuildProperties = function () {
  var _ref5 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee5() {
    var response, properties;
    return _regenerator2.default.wrap(function _callee5$(_context5) {
      while (1) {
        switch (_context5.prev = _context5.next) {
          case 0:
            _context5.next = 2;
            return fetch(url + '/admin/api/build-properties', {
              method: 'GET',
              headers: {
                'accept': 'application/json'
              }
            });

          case 2:
            response = _context5.sent;
            _context5.next = 5;
            return response.json();

          case 5:
            properties = _context5.sent;
            return _context5.abrupt('return', properties);

          case 7:
          case 'end':
            return _context5.stop();
        }
      }
    }, _callee5, this);
  }));

  return function getBuildProperties() {
    return _ref5.apply(this, arguments);
  };
}();

var getBraidAPI = exports.getBraidAPI = function () {
  var _ref6 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee6() {
    var response, braidCode;
    return _regenerator2.default.wrap(function _callee6$(_context6) {
      while (1) {
        switch (_context6.prev = _context6.next) {
          case 0:
            _context6.next = 2;
            return fetch(url + '/braid/api', {
              method: 'GET',
              headers: {
                'accept': 'application/json',
                "Authorization": 'Bearer ' + sessionStorage["corditeAccessToken"]
              }
            });

          case 2:
            response = _context6.sent;
            _context6.next = 5;
            return response.json();

          case 5:
            braidCode = _context6.sent;
            return _context6.abrupt('return', braidCode);

          case 7:
          case 'end':
            return _context6.stop();
        }
      }
    }, _callee6, this);
  }));

  return function getBraidAPI() {
    return _ref6.apply(this, arguments);
  };
}();

var deleteNodes = exports.deleteNodes = function () {
  var _ref7 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee7(nodeKey) {
    var response;
    return _regenerator2.default.wrap(function _callee7$(_context7) {
      while (1) {
        switch (_context7.prev = _context7.next) {
          case 0:
            _context7.next = 2;
            return fetch(url + '/admin/api/nodes/' + nodeKey, {
              method: 'DELETE',
              headers: {
                'accept': 'application/json',
                "Authorization": 'Bearer ' + sessionStorage["corditeAccessToken"]
              }
            });

          case 2:
            response = _context7.sent;
            return _context7.abrupt('return', response);

          case 4:
          case 'end':
            return _context7.stop();
        }
      }
    }, _callee7, this);
  }));

  return function deleteNodes(_x2) {
    return _ref7.apply(this, arguments);
  };
}();

var _jwtProcess = require('scripts/jwtProcess');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var url = document.baseURI; /*
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
});

;require.register("templates/google.analytics.pug", function(exports, require, module) {
function template(locals) {var pug_html = "", pug_mixins = {}, pug_interp;var pug_debug_filename, pug_debug_line;try {var pug_debug_sources = {"app\u002Ftemplates\u002Fgoogle.analytics.pug":"script"};
;pug_debug_line = 1;pug_debug_filename = "app\u002Ftemplates\u002Fgoogle.analytics.pug";
pug_html = pug_html + "\u003Cscript\u003E\u003C\u002Fscript\u003E";} catch (err) {pug.rethrow(err, pug_debug_filename, pug_debug_line, pug_debug_sources[pug_debug_filename]);};return pug_html;};
module.exports = template;
});

;require.register("templates/head.pug", function(exports, require, module) {
function template(locals) {var pug_html = "", pug_mixins = {}, pug_interp;var pug_debug_filename, pug_debug_line;try {var pug_debug_sources = {"app\u002Ftemplates\u002Fhead.pug":"head\n  base(href=\"${location}\u002F\")\n  meta(charset=\"utf-8\")\n  meta(name=\"viewport\" content=\"width=device-width\")\n  title='NMS'\n  link(rel=\"icon\" href=\"png\u002Ffavicon\u002Ffavicon.ico\" sizes=\"16x16 24x24 32x32 64x64\" type=\"image\u002Fvnd.microsoft.icon\")\n  link(rel=\"stylesheet\" href=\"app.css\")\n  link(href=\"https:\u002F\u002Ffonts.googleapis.com\u002Fcss?family=Montserrat:300,300i,400,400i,500,500i,600,600i,700\" rel=\"stylesheet\")\n\n  meta(name=\"title\" content=\"Cordite Stats\")\n  meta(name=\"navigation-title\" content=\"\")\n  meta(name=\"description\" content=\"\")\n  meta(name=\"keywords\" content=\"\")\n\n  meta(property=\"og:title\" content=\"Cordite Stats\")\n  meta(property=\"og:type\" content=\"website\")\n  meta(property=\"og:image\" content=\"jpg\u002Flogo-watermark-og.jpg\")\n  meta(property=\"og:url\" content=\"\")\n  meta(property=\"og:description\" content=\"\")\n\n  meta(name=\"twitter:description\" content=\"CorditeStats\")\n  meta(name=\"twitter:image:src\" content=\"jpg\u002Flogo-watermark-og.jpg\")\n  meta(name=\"twitter:card\" content=\"summary\")\n  meta(name=\"twitter:creator\" content=\"@jchrisjones\")\n  meta(name=\"twitter:site\" content=\"@we_are_cordite\")\n  meta(name=\"twitter:label1\" value=\"Network Mapping Service\")\n  meta(name=\"twitter:data1\" value=\"View nodes on the cordite network.\")\n\n  meta(name=\"theme-color\" content=\"#0A2F64\")\n  meta(name=\"msapplication-navbutton-color\" content=\"#0A2F64\")\n  meta(name=\"apple-mobile-web-app-capable\" content=\"yes\")\n  meta(name=\"apple-mobile-web-app-status-bar-style\" content=\"black\")\n\n  link(rel=\"apple-touch-icon\" href=\"png\u002Fapple-touch\u002Ftouch-icon-iphone.png\")\n  link(rel=\"apple-touch-icon\" sizes=\"152x152\" href=\"png\u002Fapple-touch\u002Ftouch-icon-ipad.png\")\n  link(rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"png\u002Fapple-touch\u002Ftouch-icon-iphone-retina.png\")\n  link(rel=\"apple-touch-icon\" sizes=\"167x167\" href=\"png\u002Fapple-touch\u002Ftouch-icon-ipad-retina.png\")\n\n  link(rel=\"icon\" sizes=\"192x192\" href=\"png\u002Fandroid\u002F192x192_icon.png\")\n  link(rel=\"manifest\" href=\"manifest.json\")\n\n  \u002F\u002F- link(rel=\"canonical\" href=\"\")"};
;pug_debug_line = 1;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Chead\u003E";
;pug_debug_line = 2;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cbase href=\"${location}\u002F\"\u003E";
;pug_debug_line = 3;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta charset=\"utf-8\"\u003E";
;pug_debug_line = 4;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"viewport\" content=\"width=device-width\"\u003E";
;pug_debug_line = 5;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Ctitle\u003E";
;pug_debug_line = 5;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + (pug.escape(null == (pug_interp = 'NMS') ? "" : pug_interp)) + "\u003C\u002Ftitle\u003E";
;pug_debug_line = 6;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"icon\" href=\"png\u002Ffavicon\u002Ffavicon.ico\" sizes=\"16x16 24x24 32x32 64x64\" type=\"image\u002Fvnd.microsoft.icon\"\u003E";
;pug_debug_line = 7;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"stylesheet\" href=\"app.css\"\u003E";
;pug_debug_line = 8;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink href=\"https:\u002F\u002Ffonts.googleapis.com\u002Fcss?family=Montserrat:300,300i,400,400i,500,500i,600,600i,700\" rel=\"stylesheet\"\u003E";
;pug_debug_line = 10;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"title\" content=\"Cordite Stats\"\u003E";
;pug_debug_line = 11;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"navigation-title\" content=\"\"\u003E";
;pug_debug_line = 12;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"description\" content=\"\"\u003E";
;pug_debug_line = 13;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"keywords\" content=\"\"\u003E";
;pug_debug_line = 15;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta property=\"og:title\" content=\"Cordite Stats\"\u003E";
;pug_debug_line = 16;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta property=\"og:type\" content=\"website\"\u003E";
;pug_debug_line = 17;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta property=\"og:image\" content=\"jpg\u002Flogo-watermark-og.jpg\"\u003E";
;pug_debug_line = 18;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta property=\"og:url\" content=\"\"\u003E";
;pug_debug_line = 19;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta property=\"og:description\" content=\"\"\u003E";
;pug_debug_line = 21;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:description\" content=\"CorditeStats\"\u003E";
;pug_debug_line = 22;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:image:src\" content=\"jpg\u002Flogo-watermark-og.jpg\"\u003E";
;pug_debug_line = 23;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:card\" content=\"summary\"\u003E";
;pug_debug_line = 24;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:creator\" content=\"@jchrisjones\"\u003E";
;pug_debug_line = 25;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:site\" content=\"@we_are_cordite\"\u003E";
;pug_debug_line = 26;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:label1\" value=\"Network Mapping Service\"\u003E";
;pug_debug_line = 27;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"twitter:data1\" value=\"View nodes on the cordite network.\"\u003E";
;pug_debug_line = 29;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"theme-color\" content=\"#0A2F64\"\u003E";
;pug_debug_line = 30;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"msapplication-navbutton-color\" content=\"#0A2F64\"\u003E";
;pug_debug_line = 31;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"apple-mobile-web-app-capable\" content=\"yes\"\u003E";
;pug_debug_line = 32;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Cmeta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\"\u003E";
;pug_debug_line = 34;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"apple-touch-icon\" href=\"png\u002Fapple-touch\u002Ftouch-icon-iphone.png\"\u003E";
;pug_debug_line = 35;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"apple-touch-icon\" sizes=\"152x152\" href=\"png\u002Fapple-touch\u002Ftouch-icon-ipad.png\"\u003E";
;pug_debug_line = 36;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"png\u002Fapple-touch\u002Ftouch-icon-iphone-retina.png\"\u003E";
;pug_debug_line = 37;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"apple-touch-icon\" sizes=\"167x167\" href=\"png\u002Fapple-touch\u002Ftouch-icon-ipad-retina.png\"\u003E";
;pug_debug_line = 39;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"icon\" sizes=\"192x192\" href=\"png\u002Fandroid\u002F192x192_icon.png\"\u003E";
;pug_debug_line = 40;pug_debug_filename = "app\u002Ftemplates\u002Fhead.pug";
pug_html = pug_html + "\u003Clink rel=\"manifest\" href=\"manifest.json\"\u003E\u003C\u002Fhead\u003E";} catch (err) {pug.rethrow(err, pug_debug_filename, pug_debug_line, pug_debug_sources[pug_debug_filename]);};return pug_html;};
module.exports = template;
});

;require.alias("buffer/index.js", "buffer");
require.alias("process/browser.js", "process");process = require('process');require.register("___globals___", function(exports, require, module) {
  
});})();require('___globals___');


//# sourceMappingURL=app.js.map