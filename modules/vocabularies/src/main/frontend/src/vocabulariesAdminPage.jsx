//
//  Licensed to the Apache Software Foundation (ASF) under one
//  or more contributor license agreements.  See the NOTICE file
//  distributed with this work for additional information
//  regarding copyright ownership.  The ASF licenses this file
//  to you under the Apache License, Version 2.0 (the
//  "License"); you may not use this file except in compliance
//  with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the License is distributed on an
//  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  KIND, either express or implied.  See the License for the
//  specific language governing permissions and limitations
//  under the License.
//

import {
  Button,
  Dialog,
  DialogContent,
  Grid,
  TextField,
  Typography,
  makeStyles
} from "@material-ui/core";

import React, {useEffect} from "react";

import VocabularyDirectory from "./vocabularyDirectory";
import OwlInstaller from "./owlInstaller";
import { fetchBioPortalApiKey, BioPortalApiKey } from "./bioportalApiKey";

const Phase = require("./phaseCodes.json");
const vocabLinks = require("./vocabularyLinks.json");

// Generates a URL to the vocabulary listing page
function generateRemoteLink(apiKey) {
  if (apiKey === null) {
    // never returned an incomplete URL without a valid key
    return "";
  }
  let url = new URL(vocabLinks["remote"]["base"]);
  url.searchParams.set("apikey", apiKey);
  Object.keys(vocabLinks["remote"]["params"]).forEach(
    (key) => {
      (key === "include" ? 
        url.searchParams.set(key, vocabLinks["remote"]["params"][key].join())
        :
        url.searchParams.set(key, vocabLinks["remote"]["params"][key])
      )
    }
  )
  return url.toString();
}

export default function VocabulariesAdminPage() {
  /* All remote vocabularies */
  const [remoteVocabList, setRemoteVocabList] = React.useState([]);
  /* Installed vocabularies */
  const [localVocabList, setLocalVocabList] = React.useState([]);

  /*
    The Phase represents the state of Vocabulary. It can be 1 of:
      1) Not Installed
      2) Installing
      3) Update Available
      4) Latest
      5) Uninstalling
  */
  const [acronymPhaseObject, setAcronymPhaseObject] = React.useState({});
  const [acronymPhaseSettersObject, setAcronymPhaseSettersObject] = React.useState({});
  const [remoteLoaded, setRemoteLoaded] = React.useState(false);
  const [localLoaded, setLocalLoaded] = React.useState(false);
  /*
    Initially the key will be fetched from a script service.
  */
  const [bioPortalApiKey, setBioPortalApiKey] = React.useState(null);

  const localLink = '/query?query=' + encodeURIComponent(`select * from [lfs:Vocabulary]`);

  function processLocalVocabList(vocabList) {
    setLocalVocabList(vocabList);
    setLocalLoaded(true);
  }

  function processRemoteVocabList(vocabList) {
    setRemoteVocabList(vocabList);
    setRemoteLoaded(true);
  }

  function addSetter(acronym, setFunction, type) {
    var copy = acronymPhaseSettersObject;
    if (copy.hasOwnProperty(acronym)) {
      copy[acronym][type] = setFunction;
    } else {
      var temp = {};
      temp[type] = setFunction;
      copy[acronym] = temp;
    }
    setAcronymPhaseSettersObject(copy);
  }

  function setPhase(acronym, phase) {
    const setters = acronymPhaseSettersObject[acronym];
    if (setters.hasOwnProperty("local")) {
      setters["local"](phase);
    }
    if (setters.hasOwnProperty("remote")) {
      setters["remote"](phase);
    }
    // update acronyms object
    let phases = acronymPhaseObject;
    phases[acronym] = phase;
    setAcronymPhaseObject(phases);
  }

  /* Set phases for the installed local vocabs once all vocabs are loaded
     All others have the default not installed phase
  */
  function setPhases() {
    var tempAcronymPhaseObject = {};

    localVocabList.map((vocab) => {
      tempAcronymPhaseObject[vocab.acronym] = Phase["Latest"]; // default

      let remoteVocab = remoteVocabList.find(item => item.acronym == vocab.acronym);
      if (remoteVocab && remoteVocab.released) {
        const remoteReleaseDate = new Date(remoteVocab.released);
        const localInstallDate = new Date(vocab.released);
        if (remoteReleaseDate > localInstallDate) {
          tempAcronymPhaseObject[vocab.acronym] = Phase["Update Available"];
        }
      }
    });
    setAcronymPhaseObject(tempAcronymPhaseObject);

  }

  useEffect(() => {
    localLoaded && remoteLoaded && setPhases();
  }, [localLoaded, remoteLoaded])

  function updateLocalList(action, vocab) {
    const acronym = vocab.acronym;

    if (action === "add") {
      var tempLocalVocabList = localVocabList.slice();
      tempLocalVocabList.push(vocab);
      setLocalVocabList(tempLocalVocabList);

    } else if (action === "remove") {
      var copy = acronymPhaseSettersObject;
      delete copy[acronym]["local"];
      setAcronymPhaseSettersObject(copy);

      let phases = acronymPhaseObject;
      delete phases[acronym];
      setAcronymPhaseObject(phases);

      setLocalVocabList(localVocabList.filter((vocabulary) => vocabulary.acronym != acronym));
    }
  }

  function handleErrorModal(isError) {
    // show modal
    setError(isError);
  }

  function updateBioPortalApiKey(apiKey) {
    if (apiKey) {
      setBioPortalApiKey(apiKey);
      setRemoteLoaded(false);
    } else if (apiKey == "") {
      setBioPortalApiKey(apiKey);
      setRemoteLoaded(true);
    } else {
      setRemoteLoaded(true);
    }
  }

  return (
    <Grid container direction="column" spacing={4} justify="space-between">
      <Grid item>
        <Typography variant="h2">
          Vocabularies
        </Typography>
      </Grid>

      <Grid item>
        <Typography variant="h6">
          Installed
        </Typography>
      </Grid>
      { localVocabList.length == 0 &&
          <Grid item>
            <Typography color="textSecondary">No local vocabularies are installed yet.</Typography>
          </Grid>
      }
      <VocabularyDirectory 
        type="local"
        link={localLink}
        vocabList={localVocabList}
        setVocabList={processLocalVocabList}
        acronymPhaseObject={acronymPhaseObject}
        updateLocalList={updateLocalList}
        addSetter={addSetter}
        setPhase={setPhase}
        apiKey={bioPortalApiKey}
        loaded={localLoaded}
      />

      <OwlInstaller updateLocalList={updateLocalList} reloadVocabList={() => {setLocalLoaded(false);}}/>

      <BioPortalApiKey
        bioPortalApiKey={bioPortalApiKey}
        updateKey={updateBioPortalApiKey}
      />

      <VocabularyDirectory 
        type="remote"
        link={generateRemoteLink(bioPortalApiKey)}
        vocabList={remoteVocabList}
        setVocabList={processRemoteVocabList}
        acronymPhaseObject={acronymPhaseObject}
        setPhase={setPhase}
        updateLocalList={updateLocalList}
        addSetter={addSetter}
        apiKey={bioPortalApiKey}
        loaded={remoteLoaded}
      />
    </Grid>
  );
}
