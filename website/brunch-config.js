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
exports.files = {
  javascripts: {
    joinTo: {
      'vendor.js': /^(?!app)/,
      'app.js': /^app/
    }
  },
  stylesheets: {joinTo: 'app.css'}
};

exports.watcher = {
  awaitWriteFinish: true,
  usePolling: true
}

exports.plugins = {
  babel: {
    "presets": ["env", "react", "stage-0"],
    "plugins": ["transform-class-properties", "transform-async-to-generator", "transform-runtime"]
  },
  pug:{
    preCompile: true,
    preCompilePattern: /.html.pug$/,
    pugRuntime: require('path').resolve('.', 'vendor', 'pug_runtime.js'),
    globals: ['App']
  },
  autoReload: {
    enabled: {
      css: true,
      js: true,
      assets: true
    },
    delay: 200,
  }
};