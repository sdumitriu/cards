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

import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import {
  Grid,
  Button,
  IconButton,
  TextField,
  InputAdornment,
  FormControlLabel,
  Popover,
  Tooltip,
  Switch,
  Typography,
  withStyles
} from "@material-ui/core";

import QuestionnaireStyle from '../questionnaire/QuestionnaireStyle';
import EditorInput from "./EditorInput";
import QuestionComponentManager from "./QuestionComponentManager";
import MarkdownText from "./MarkdownText";
import CloseIcon from '@material-ui/icons/Close';
import AddCircleIcon from '@material-ui/icons/AddCircle';
import EditIcon from '@material-ui/icons/Edit';
import NotesIcon from '@material-ui/icons/Notes';
import { stringToHash } from "../escape.jsx";
import DragIndicatorIcon from '@material-ui/icons/DragIndicator';
import { DragDropContext, Droppable, Draggable } from "react-beautiful-dnd";
import ComposedIcon from "../components/ComposedIcon.jsx";

let extractSortedOptions = (data) => {
  return Object.values(data).filter(value => value['jcr:primaryType'] == 'cards:AnswerOption'
                                             && !value.notApplicable
                                             && !value.noneOfTheAbove)
                            .slice()
                            .sort((option1, option2) => (option1.defaultOrder - option2.defaultOrder));
}

let AnswerOptions = (props) => {
  const { objectKey, data, path, saveButtonRef, classes } = props;
  let [ options, setOptions ] = useState(extractSortedOptions(data));
  let [ deletedOptions, setDeletedOptions ] = useState([]);
  let [ tempValue, setTempValue ] = useState(''); // Holds new, non-committed answer options
  let [ isDuplicate, setIsDuplicate ] = useState(false);
  let [ isNADuplicate, setIsNADuplicate ] = useState(false);
  let [ isNoneDuplicate, setIsNoneDuplicate ] = useState(false);
  let [ descriptionIndex, setDescriptionIndex ] = useState(null);
  let [ description, setDescription ] = useState('');
  let [ descriptionAnchorEl, setDescriptionAnchorEl ] = useState(null);
  let [ descriptionLabel, setDescriptionLabel ] = useState('');
  let [ isSpecialOption, setIsSpecialOption ] = useState(false);

  const notApplicable  = Object.values(data).find(option => option['jcr:primaryType'] == 'cards:AnswerOption' && option.notApplicable);
  const noneOfTheAbove = Object.values(data).find(option => option['jcr:primaryType'] == 'cards:AnswerOption' && option.noneOfTheAbove);

  let [ notApplicableOption, setNotApplicableOption ] = useState(notApplicable || {"value" : "notApplicable",
                                                                                   "label" : "None",
                                                                                   "notApplicable" : false,
                                                                                   "@path" : path + "/None"});
  let [ noneOfTheAboveOption, setNoneOfTheAboveOption ] = useState(noneOfTheAbove || {"value": "noneOfTheAbove",
                                                                                      "label" : "None of the above",
                                                                                      "noneOfTheAbove" : false,
                                                                                      "@path" : path + "/NoneOfTheAbove"});

  const specialOptionsInfo = [
    {
      tooltip : "This option behaves as 'None' or 'N/A', and unselects/removes all other options upon selection.",
      switchTooltip: "Enable N/A",
      data : notApplicableOption,
      setter : setNotApplicableOption,
      label: "notApplicable",
      defaultOrder: 0,
      isDuplicate: isNADuplicate,
      duplicateSetter: setIsNADuplicate
    },
    {
      tooltip : "This option behaves as 'None of the above'. When selected, it removes all existing selections except those entered by the user in the input, if applicable.",
      switchTooltip: "Enable 'None of the above'",
      data : noneOfTheAboveOption,
      setter : setNoneOfTheAboveOption,
      label: "noneOfTheAbove",
      defaultOrder: 99999,
      isDuplicate: isNoneDuplicate,
      duplicateSetter: setIsNoneDuplicate
    }
  ]

  let getItemStyle = (isDragging, draggableStyle) => ({
    // change background colour if dragging
    border: isDragging ? "2px dashed" : "none",
    borderColor: isDragging ? "lightblue" : "",
    ...draggableStyle
  });

  let onDragEnd = (result) => {
    // dropped outside the list
    if (!result.destination) {
      return;
    }
    let oldOptions = options.slice();
    const [removed] = oldOptions.splice(result.source.index, 1);
    oldOptions.splice(result.destination.index, 0, removed);
    setOptions(oldOptions);
  }

  // Clear local state when data changes
  useEffect(() => {
    setOptions(extractSortedOptions(data));
    setDeletedOptions([]);
    setTempValue('');
    setIsDuplicate(false);
  }, [data])

  let deleteOption = (index) => {
    setDeletedOptions(old => {
      let newDeletedOptions = old.slice();
      newDeletedOptions.push(options[index]);
      return newDeletedOptions;
    });

    setOptions(oldOptions => {
      let newOptions = oldOptions.slice();
      newOptions.splice(index, 1);
      return newOptions;
    });
  }

  let validateOption = (optionInput, setter, specialOption) => {
    if (optionInput) {
      setter(false);
      let inputs = (optionInput || '').trim().split(/\s*=\s*(.*)/);
      let allOptions = options.slice();
      specialOption != notApplicableOption && notApplicableOption.notApplicable && allOptions.push(notApplicableOption);
      specialOption != noneOfTheAboveOption && noneOfTheAboveOption.noneOfTheAbove && allOptions.push(noneOfTheAboveOption);
      let duplicateOption = allOptions.find( option => option.value === inputs[0] || inputs[1] && (option.label === inputs[1]));
      duplicateOption && setter(true);
      return !!duplicateOption;
    }
    return false;
  }

  let handleSpecialInputOption = (option, optionInput) => {
    let duplicate = validateOption(optionInput, option.duplicateSetter, option.data);
    if (optionInput && !duplicate) {
      let inputs = (optionInput || '').trim().split(/\s*=\s*(.*)/);
      option.setter({ ...option.data, "value": inputs[0].trim(), "label": inputs[1] ? inputs[1].trim() : ""});
    }
  }

  let handleInputOption = (optionInput) => {
    if (optionInput && !isDuplicate) {
      // The text entered on each line should be split
      // by the first occurrence of the separator = if the separator exists
      // e.g. F=Female as <value> = <label>
      let inputs = (optionInput || '').trim().split(/\s*=\s*(.*)/);
      let newOption = {};
      newOption.value = inputs[0].trim();
      newOption["@path"] = path + "/AnswerOption" + stringToHash(newOption.value);
      newOption.label = inputs[1] ? inputs[1].trim() : "";

      setOptions(oldValue => {
        var value = oldValue.slice();
        value.push(newOption);
        return value;
      });
    }

    tempValue && setTempValue('');
    setIsDuplicate(false);

    // Have to manually invoke submit with timeout to let re-rendering of adding new answer option complete
    // Cause: Calling onBlur and mutating state can cause onClick for form submit to not fire
    // Issue details: https://github.com/facebook/react/issues/4210
    if (event?.relatedTarget?.type == "submit") {
      const timer = setTimeout(() => {
        saveButtonRef?.current?.click();
      }, 500);
    }
  }

  let generateDescriptionIcon = (item, index, isSpecialOptn) => {
    return (
      <Tooltip title="Description">
        <IconButton
          onClick={(event) => {
                                setDescriptionAnchorEl(event.currentTarget);
                                setDescriptionIndex(index);
                                setDescriptionLabel(item.label || item.value);
                                setIsSpecialOption(isSpecialOptn);
                                setDescription(item.description);
                              }
                   }
          className={isSpecialOptn ? classes.specialOptionButton : classes.answerOptionButton}
        >
          <ComposedIcon
              size="large"
              MainIcon={NotesIcon}
              ExtraIcon={!item.description ? AddCircleIcon : EditIcon}/>
        </IconButton>
      </Tooltip>
    )
  }
  
  let generateSpecialOptions = (index) => {
    let option = specialOptionsInfo[index];
    return (
    <Grid container
       direction="row"
       justify="space-between"
       alignItems="stretch"
       onClick={(event) => option.setter({ ...option.data, [option.label]: true})}
       >
      <Grid item xs={9}>
      <Tooltip title={option.tooltip}>
        <TextField
          disabled={!option.data[option.label]}
          label={option.tootltip}
          error={option.data[option.label] && option.isDuplicate}
          helperText={option.isDuplicate ? 'duplicated value or label' : ''}
          className={classes.answerOptionInput}
          defaultValue={option.data.label? option.data.value + " = " + option.data.label : option.data.value}
          onChange={(event) => { handleSpecialInputOption(option, event.target.value); }}
        />
      </Tooltip>
      </Grid>
      <Grid item xs={3}>
      <Tooltip title={option.switchTooltip} className={classes.specialOptionSwitch}>
        <FormControlLabel
          control={
            <Switch
              checked={!!option.data[option.label]}
              onChange={(event) => option.setter({ ...option.data, [option.label]: event.target.checked})}
              color="primary"
              />
          }
        />
      </Tooltip>
      {generateDescriptionIcon(option.data, index, true)}
      { option.data[option.label]
        ?
        <>
          <input type='hidden' name={`${option.data['@path']}/jcr:primaryType`} value={'cards:AnswerOption'} />
          <input type='hidden' name={`${option.data['@path']}/value`} value={option.data.value} />
          <input type='hidden' name={`${option.data['@path']}/label`} value={option.data.label} />
          <input type='hidden' name={`${option.data['@path']}/${option.label}`} value={option.data[option.label]} />
          <input type='hidden' name={`${option.data['@path']}/defaultOrder`} value={option.defaultOrder} />
          <input type="hidden" name={`${option.data['@path']}/description`} value={option.data.description || ''} />
        </>
        :
        <input type='hidden' name={`${option.data['@path']}@Delete`} value="0" />
      }
      </Grid>
    </Grid>
    )
  }
  
  let handleClose = () => {
    setDescriptionAnchorEl(null);
    setDescriptionIndex(null);
    setDescriptionLabel('');
    setIsSpecialOption(false);
    setDescription('');
  }

  let onDescriptionPopoverClose = () => {
    // update corresponding option description
    if (isSpecialOption) {
      specialOptionsInfo[descriptionIndex].setter({ ...specialOptionsInfo[descriptionIndex].data, "description": description});
    } else {
      setOptions(oldValue => {
        var value = oldValue.slice();
        value[descriptionIndex].description = description;
        return value;
      });
    }
    handleClose();
  }

  return (
    <EditorInput name={objectKey}>
      { deletedOptions.map((value, index) =>
        <input type='hidden' name={`${value['@path']}@Delete`} value="0" key={value['@path']} />
      )}
      { generateSpecialOptions(0) }
      <DragDropContext onDragEnd={onDragEnd}>
        <Droppable droppableId="droppable">
          {(provided, snapshot) => (
            <div
              {...provided.droppableProps}
              ref={provided.innerRef}
            >
              { options.map((value, index) =>
                <Draggable key={value.value} draggableId={value.value} index={index}>
                  { (provided, snapshot) => (
                    <Grid container
                      direction="row"
                      justify="space-between"
                      alignItems="stretch"
                      className={classes.answerOption}
                      key={value.value}
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      style={getItemStyle(
                          snapshot.isDragging,
                          provided.draggableProps.style
                      )}
                    >
                      <Grid item xs={1}>
                        <Tooltip title="Drag to reorder">
                          <IconButton {...provided.dragHandleProps} className={classes.optionsDragIndicator}>
                            <DragIndicatorIcon />
                          </IconButton>
                        </Tooltip>
                      </Grid>
                      <Grid item xs={8}>
                        <input type='hidden' name={`${value['@path']}/jcr:primaryType`} value={'cards:AnswerOption'} />
                        <input type='hidden' name={`${value['@path']}/label`} value={value.label} />
                        <input type='hidden' name={`${value['@path']}/value`} value={value.value} />
                        <input type='hidden' name={`${value['@path']}/defaultOrder`} value={index+1} />
                        <input type="hidden" name={`${value['@path']}/description`} value={value.description || ''} />
                        <TextField
                          InputProps={{
                            readOnly: true,
                          }}
                          className={classes.answerOptionInput}
                          defaultValue={value.label? value.value + " = " + value.label : value.value}
                          multiline
                        />
                      </Grid>
                      <Grid item xs={3}>
                        <Tooltip title="Delete option">
                          <IconButton onClick={() => { deleteOption(index); }} className={classes.answerOptionButton}>
                            <CloseIcon/>
                          </IconButton>
                        </Tooltip>
                        {generateDescriptionIcon(value, index, false)}
                      </Grid>
                    </Grid>
                  ) }
                </Draggable>
              )}
              {provided.placeholder}
            </div>
          )}
        </Droppable>
      </DragDropContext>
      <TextField
        fullWidth
        className={classes.newOptionInput}
        value={tempValue}
        error={isDuplicate}
        label="value OR value=label (e.g. F=Female)"
        helperText={isDuplicate ? 'Duplicated value or label' : 'Press ENTER to add a new line'}
        onChange={(event) => { setTempValue(event.target.value); validateOption(event.target.value, setIsDuplicate); }}
        onBlur={(event) => { handleInputOption(event.target.value); }}
        inputProps={Object.assign({
          onKeyDown: (event) => {
            if (event.key == 'Enter') {
              // We need to stop the event so that it doesn't trigger a form submission
              event.preventDefault();
              event.stopPropagation();
              handleInputOption(event.target.value);
            }
          }
        })}
        multiline
        />
      { generateSpecialOptions(1) }
      <Popover
        disableBackdropClick
        disableEscapeKeyDown
        open={Boolean(descriptionAnchorEl)}
        anchorEl={descriptionAnchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'center',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'center',
        }}
        className={classes.descriptionPopover}
      >
        <Typography variant="h6" className={classes.descriptionPopoverTitle} >
          {`Description for "${descriptionLabel}"`}
        </Typography>
        { descriptionIndex != null &&
          <MarkdownText
            value={description}
            onChange={setDescription}
          />
        }
        <Button
          variant='contained'
          color='default'
          onClick={handleClose}
          className={classes.descriptionPopoverButton}
        >
          Cancel
        </Button>
        <Button
          variant='contained'
          color='primary'
          onClick={onDescriptionPopoverClose}
          className={classes.descriptionPopoverButton}
        >
          Done
        </Button>
    </Popover>
    </EditorInput>
  )
}

AnswerOptions.propTypes = {
  data: PropTypes.object.isRequired
};

var StyledAnswerOptions = withStyles(QuestionnaireStyle)(AnswerOptions);
export default StyledAnswerOptions;

QuestionComponentManager.registerQuestionComponent((definition) => {
  if (definition === "options") {
    return [StyledAnswerOptions, 50];
  }
});
