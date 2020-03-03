/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

import React from "react";

import { withStyles } from "@material-ui/core/styles";

import { Avatar, Button, Card, CardHeader, CardContent, Grid, Table, TableCell, TableBody, TableHead, TableRow } from "@material-ui/core";

import userboardStyle from '../userboardStyle.jsx';
import CreateGroupDialogue from "./creategroupdialogue.jsx";
import DeleteGroupDialogue from "./deletegroupdialogue.jsx";
import AddUserToGroupDialogue from "./addusertogroupdialogue.jsx";

import MaterialTable from 'material-table';

const GROUP_URL="/system/userManager/group/";

class GroupsManager extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      groups: [],
      selectedUsers: [],
      currentGroupUsers: [],

      currentGroupName: "",
      currentGroupIndex: -1,

      deployCreateGroup: false,
      deployDeleteGroup: false,
      deployAddGroupUsers: false,
      groupUsersLoaded: false
    };
  }

  clearSelectedGroup () {
    this.setState(
      {
        currentGroupName: "",
        currentGroupIndex: -1,
        selectedUsers: [],
      }
    );
  }

  addName(name) {
    return { name }
  }

  handleLoadGroups () {
    this.clearSelectedGroup();

    fetch("/home/groups.json",
      {
        method: 'GET',
        headers: {
          'Authorization' : 'Basic' + btoa('admin:admin')
        }
    })
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      this.setState({ groups: data.rows });
    })
    .catch((error) => {
      console.log(error);
    })
  }

  handleSelectRowClick(rows) {
    let chosens = rows.map((row) => row.name);
    this.setState({ selectedUsers: chosens });
  }

  handleRemoveUsers() {
    let formData = new FormData();

    var i;
    for (i = 0; i < this.state.selectedUsers.length; ++i) {
      formData.append(':member@Delete', this.state.selectedUsers[i]);
    }

    fetch(GROUP_URL + this.state.currentGroupName + ".update.html",
      {
        method: 'POST',
        credentials: 'include',
        body: formData
      })
      .then(() => {
        this.handleReload();
      })
      .catch((error) => {
        console.log(error);
      });
  }

  componentWillMount () {
    this.handleLoadGroups();
  }

  handleGroupRowClick(index, name) {
    this.setState(
      {
        currentGroupName: name,
        currentGroupIndex: index
      }
    );
  }

  handleGroupDeleteClick(index, name) {
    this.handleGroupRowClick(index, name);
    this.setState({deployDeleteGroup: true});
  }

  handleReload () {
    this.handleLoadGroups();
  }

  render() {
    const { classes } = this.props;
    const headerBackground = this.props.theme.palette.grey['200'];
    return (
      <div>
        <CreateGroupDialogue isOpen={this.state.deployCreateGroup} handleClose={() => {this.setState({deployCreateGroup: false});}} reload={() => this.handleReload()} />
        <DeleteGroupDialogue isOpen={this.state.deployDeleteGroup} handleClose={() => {this.setState({deployDeleteGroup: false});}} name={this.state.currentGroupName} reload={() => this.handleReload()} />
        <AddUserToGroupDialogue isOpen={this.state.deployAddGroupUsers} handleClose={() => {this.setState({deployAddGroupUsers: false});}} name={this.state.currentGroupName} groupUsers={this.state.currentGroupUsers} reload={() => this.handleReload()} />
        <div>
          <MaterialTable
            title="Group list"
            style={{ boxShadow : 'none' }}
            options={{
              actionsColumnIndex: -1,
              headerStyle: {backgroundColor: headerBackground},
              emptyRowsWhenPaging: false
            }}
            columns={[
              { title: 'Avatar', field: 'imageUrl', render: rowData => <Avatar src={rowData.imageUrl} className={classes.info}>{rowData.name.charAt(0)}</Avatar> },
              { title: 'Name', field: 'name', cellStyle: {textAlign: 'left'} },
              { title: 'Members', field: 'members', type: 'numeric', cellStyle: {textAlign: 'left'}, headerStyle: {textAlign: 'left', flexDirection: 'initial'} },
              { title: 'Declared Members', field: 'declaredMembers', type: 'numeric', cellStyle: {textAlign: 'left'}, headerStyle: {textAlign: 'left', flexDirection: 'initial'} },
            ]}
            data={this.state.groups}
            actions={[
              {
                icon: 'delete',
                tooltip: 'Delete Group',
                onClick: (event, rowData) => this.handleGroupDeleteClick(rowData.tableData.id, rowData.name)
              },
              {
                icon: 'add_circle',
                tooltip: 'Create New Group',
                isFreeAction: true,
                onClick: (event) => this.setState({deployCreateGroup: true})
              }
             ]}
            onRowClick={(event, rowData, togglePanel) => {this.handleGroupRowClick(rowData.tableData.id, rowData.name); togglePanel()}}
            detailPanel={rowData => {
                const group = rowData || this.state.groups[this.state.currentGroupIndex];

                return (
                <div>
                    <Card className={classes.cardRoot}>
                      <CardContent>
                        {
                          <div>
                            <MaterialTable
			                  title="Group users"
			                  style={{ boxShadow : 'none' }}
			                  options={{
			                    
			                  }}
			                  options={{
			                    emptyRowsWhenPaging: false,
			                    selection: true,
			                    showSelectAllCheckbox : false,
			                    showTextRowsSelected: false,
			                    headerStyle: {backgroundColor: headerBackground},
			                    selectionProps: rowData => ({
			                        color: 'primary'
			                      })
			                  }}
			                  columns={[
			                    { title: 'User Name', field: 'name' }
			                  ]}
				              data={query =>
						          new Promise((resolve, reject) => {
						            let url = GROUP_URL + group.principalName + ".1.json"
						            fetch(url, {
							            method: 'GET',
							            credentials: 'include'
							        })
						            .then(response => response.json())
						            .then(result => {
						                let groupUsers = result?.members?.map((n) => this.addName(n?.split('/').pop()));
						                let begin = query.pageSize*query.page;
						                let end = Math.min((query.page+1)*query.pageSize, result.members.length);
						                let pageUsers = groupUsers.slice(begin, end);

						                this.setState({currentGroupUsers: groupUsers});

						                resolve({
						                  data: pageUsers,
						                  page: query.page,
						                  totalCount: result.members.length
						                })
						            })
								    .catch((error) => {
								        console.log(error);
								    });
								 })
							  }
			                  onSelectionChange={(rows) => {this.handleSelectRowClick(rows)}}
			                />
                          </div>
                        }
                        <Grid container className={classes.cardActions}>
                          <Button
                            variant="contained"
                            color="primary"
                            size="small"
                            className={classes.containerButton}
                            onClick={() => {this.setState({currentGroupName: group.principalName, deployAddGroupUsers: true});}}
                          >
                            Add User to Group
                          </Button>
                          <Button
                            variant="contained"
                            color="secondary"
                            size="small"
                            disabled={this.state.selectedUsers.length == 0}
                            onClick={() => {this.setState({currentGroupName: group.principalName}); this.handleRemoveUsers();}}
                          >
                            Remove User from Group
                          </Button>
                        </Grid>
                      </CardContent>
                    </Card>
                    </div>
                )
            }}
          />
        </div>
      </div>
    );
  }
}

export default withStyles (userboardStyle, {withTheme: true})(GroupsManager);
