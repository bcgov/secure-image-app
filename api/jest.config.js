//
// Repo Mountie
//
// Copyright © 2018 Province of British Columbia
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Created by Jason Leach on 2018-10-12.
//

module.exports = {
  // collectCoverage: true,
  collectCoverageFrom: [
    "src/**/*.{js,jsx}",
    "!**/node_modules/**",
    "!**/vendor/**",
  ],
  moduleFileExtensions: ["js", "jsx", "json", "node"],
  moduleDirectories: ["node_modules"],
  modulePathIgnorePatterns: ["<rootDir>/build/"],
  moduleNameMapper: {
    axios: "axios/dist/node/axios.cjs",
  },
  testEnvironment: "node",
  testRegex: ["(/__tests__/.*|(\\.|/)(test|spec))\\.[js]sx?$"],
  testPathIgnorePatterns: [
    "/node_modules/",
    "/.cache/",
    "/scripts/",
    "/build/",
    "/dist/",
    "/__tests__/src/",
  ],
  verbose: true,
};
