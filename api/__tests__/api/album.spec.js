//
// Code Signing
//
// Copyright © 2018 Province of British Columbia
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
// Created by Jason Leach on 2018-07-20.
//

import fs from "fs";
import path from "path";
import { default as request } from "supertest"; // eslint-disable-line
import app from "../../src";

jest.mock("../../src/libs/archive");

const sample = path.join(__dirname, "sample.jpg");
const archive = path.join(__dirname, "archive.zip");

beforeAll(() => {
  const buf = Buffer.from("7468697320697320612074c3a97374", "hex");
  fs.writeFileSync(sample, buf);
  fs.writeFileSync(archive, buf);
});

afterAll(() => {
  fs.unlinkSync(sample);
  fs.unlinkSync(archive);
});

describe("Create album", () => {
  test("Create album should fail with a bad token", async () => {
    await request(app)
      .post("/v1/album")
      .set("Authorization", "Bearer 123cake123")
      .expect(401)
      .expect("Content-Type", /json/);
  });

  test("Create album should provide an album ID", async () => {
    const response = await request(app)
      .post("/v1/album")
      .set("Authorization", "Bearer 123bacon123")
      .expect(200)
      .expect("Content-Type", /json/);
    expect(response.body.id).not.toBeUndefined();
  });
});

describe("Add file to album", () => {
  test("Add file to album should fail with a bad token", async () => {
    await request(app)
      .post("/v1/album/abc123")
      .set("Authorization", "Bearer 123cake123")
      .expect(401)
      .expect("Content-Type", /json/);
  });

  test("Add file to album should fail with no file attachment", async () => {
    await request(app)
      .post("/v1/album/abc123")
      .set("Authorization", "Bearer 123bacon123")
      .expect(400)
      .expect("Content-Type", /json/);
  });

  test("Add file to album should succeed when all parameters are valid", async () => {
    const response = await request(app)
      .post("/v1/album/abc123")
      .set("Authorization", "Bearer 123bacon123")
      .attach("file", sample)
      .expect(200)
      .expect("Content-Type", /json/);
    expect(response.body.id).not.toBeUndefined();
  });
});

describe("Fetch album details", () => {
  test("Fetch album should fail with a bad token", async () => {
    await request(app)
      .get("/v1/album/abc123")
      .set("Authorization", "Bearer 123cake123")
      .expect(401)
      .expect("Content-Type", /json/);
  });

  test("Fetch should fail with an invalid album name", async () => {
    await request(app)
      .get("/v1/album/abc123")
      .set("Authorization", "Bearer 123bacon123")
      .query({ name: "sparkle!!" })
      .expect(400)
      .expect("Content-Type", /json/);
  });

  test.skip("Fetch should provide an album download URL", async () => {
    // await request(app)
    //   .get("/v1/album/abc123")
    //   .set("Authorization", "Bearer 123bacon123")
    //   .query({ name: "sparkle" })
    //   .expect(200)
    //   .expect("Content-Type", /json/);
  });
});

describe("Add annotations to an album", () => {
  test("Add comment should fail with a bad token", async () => {
    await request(app)
      .post("/v1/album/abc123/note")
      .set("Authorization", "Bearer 123cake123")
      .expect(401)
      .expect("Content-Type", /json/);
  });

  test("Add comment fail with without fields", async () => {
    await request(app)
      .post("/v1/album/abc123/note")
      .set("Authorization", "Bearer 123bacon123")
      .expect(400)
      .expect("Content-Type", /json/);
  });

  test("Add comment succeed with just an album name", async () => {
    await request(app)
      .post("/v1/album/abc123/note")
      .set("Authorization", "Bearer 123bacon123")
      .set("Content-Type", "application/json")
      .send({
        albumName: "bacon",
      })
      .expect(200)
      .expect("Content-Type", /json/);
  });

  test("Add comment succeed with just a comment", async () => {
    await request(app)
      .post("/v1/album/abc123/note")
      .set("Authorization", "Bearer 123bacon123")
      .set("Content-Type", "application/json")
      .send({
        comment: "Hello Comment",
      })
      .expect(200)
      .expect("Content-Type", /json/);
  });

  test("Add comment succeed with all fields", async () => {
    await request(app)
      .post("/v1/album/abc123/note")
      .set("Authorization", "Bearer 123bacon123")
      .set("Content-Type", "application/json")
      .send({
        albumName: "bacon",
        comment: "Hello Comment",
      })
      .expect(200)
      .expect("Content-Type", /json/);
  });
});

describe("Download an album", () => {
  test("Download album should redirect for authentication", async () => {
    await request(app)
      .get("/v1/album/abc123/download/archive.zip")
      .expect(302)
      .expect("Content-Type", /plain/);
  });
});
