let queryBlock = new Vue({
    el: "#queryBlock",
    data: {
        form: {
            name: '',
            mobilePhone: '',
            loginName: '',
            email: '',
            status: ''
        }
    },
    methods: {
        query() {
            datagrid.query(this.form);
        },
        updateStatus(status) {
            let vm = this;
            let hasCheckedData = datagrid.$refs.datagrid.hasCheckedData;
            if (hasCheckedData.length > 0) {
                let ids = hasCheckedData.map(item => {
                    return item.id;
                });
                http.post(BD.realPath + "/permission/account/updateStatus", {
                    ids: ids.join(","),
                    status: status
                }).then(response => {
                    const {rtnMsg} = response;
                    vm.$message.success({
                        message: rtnMsg || "操作成功"
                    });
                    vm.query();
                    datagrid.$refs.datagrid.hasCheckedData = [];
                })
            } else {
                this.$message({
                    type: "warning",
                    message: "请至少选择一条数据"
                })
            }
        },
        add() {
            dialog.form = {
                id: '',
                name: '',
                mobilePhone: '',
                loginName: '',
                email: ''
            };
            dialog.formShow = true;
        },
        bindRoles() {
            let hasCheckedData = datagrid.$refs.datagrid.hasCheckedData;
            if (hasCheckedData.length === 0) {
                this.$message.warning("至少选择一个账号绑定");
                return false;
            }
            if (hasCheckedData.length > 1) {
                this.$message.warning("只能选择一个账号绑定");
                return false;
            }
            bindDialog.accountId = hasCheckedData[0].id;
            bindDialog.query();
            bindDialog.formShow = true;
        }
    }
});

let datagrid = new Vue({
    el: "#datagrid",
    data: {
        uri: BD.realPath + '/permission/account/loadAccounts',
        height: null,
        columns: [
            {
                label: ' ',
                type: 'selection'
            },
            {
                label: '姓名',
                prop: 'name'
            },
            {
                label: '手机号',
                prop: 'mobilePhone'
            },
            {
                label: '电子邮件',
                prop: 'email'
            },
            {
                label: '用户名',
                prop: 'loginName'
            },
            {
                label: '状态',
                prop: 'status',
                formatter(a, b, c) {
                    if (c === 'enabled') {
                        return "启用"
                    } else if (c === 'disable') {
                        return "禁用"
                    }
                }
            }
        ],
        actions: [
            {
                text: 'edit',
                label: '编辑',
                callback(row) {
                    let data = JSON.parse(JSON.stringify(row));
                    dialog.form = {
                        id: data.id,
                        name: data.name,
                        mobilePhone: data.mobilePhone,
                        loginName: data.loginName,
                        email: data.email,
                    };
                    dialog.title = "编辑";
                    dialog.formShow = true;
                },
                checkShow(row) {
                    return true
                }
            },
            {
                text: 'reset',
                label: '重置密码',
                callback(row) {
                    datagrid.$confirm('确认是否继续?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        http.post(BD.realPath + "/permission/account/resetPwd", {id: row.id}).then(response => {
                            const {rtnMsg} = response;
                            datagrid.$message({
                                type: 'success',
                                message: rtnMsg || "操作成功"
                            });
                        }).catch(error => {
                            console.log(error);
                            datagrid.$message.error("系统异常,请联系管理员");
                        })
                    }).catch(() => {
                        datagrid.$message({
                            type: 'info',
                            message: '已取消操作'
                        });
                    });
                },
                checkShow(row) {
                    return true
                }
            }
        ]
    },
    methods: {
        async query(params) {
            await this.$refs.datagrid.query(Object.assign(params, {}));
        }
    },
    async mounted() {
        calHeight(this);
        this.query(queryBlock.form);
    }
});

let dialog = new Vue({
    el: "#dialog",
    data: {
        form: {
            id: '',
            name: '',
            mobilePhone: '',
            loginName: '',
            email: ''
        },
        rules: {
            name: [
                {required: true, message: '该项不能为空', trigger: 'blur'}
            ],
            mobilePhone: [
                {required: true, message: '该项不能为空', trigger: 'blur'},
                {validator: checkPhone, trigger: 'blur'}
            ],
            loginName: [
                {required: true, message: '该项不能为空', trigger: 'blur'}
            ],
            email: [
                {required: true, message: '该项不能为空', trigger: 'blur'},
                {type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change']}
            ]
        },
        title: '新增',
        formShow: false
    },
    methods: {
        closeDialog() {
            this.$refs['addForm'].resetFields();
            this.title = '';
            this.form = {
                id: '',
                name: '',
                mobilePhone: '',
                loginName: '',
                email: ''
            };
            this.formShow = false;
        },
        save(form) {
            this.$refs[form].validate((valid, fileds) => {
                if (valid) {
                    let uri = dialog.title === '编辑' ? '/permission/account/editAccount' : '/permission/account/addAccount';
                    http.post(BD.realPath + uri, this.form).then(response => {
                        const {rtnFlag, rtnMsg} = response;
                        this.$message.success(rtnMsg || "操作成功");
                        this.formShow = false;
                        datagrid.$mount();
                    })
                } else {
                    console.log(fileds)
                }
            });
        }
    }
});

let bindDialog = new Vue({
    el: "#bindDialog",
    data: {
        formShow: false,
        height: 200,
        tableData: [],
        loading: false,
        checkedRoleData: [],
        accountId: ''
    },
    methods: {
        handleSelectAll(val) {
            let v = this;
            //remove
            if (val.length === 0) {
                for (let i = 0; i < v.tableData.length; i++) {
                    for (let j = 0; j < v.checkedRoleData.length; j++) {
                        if (v.checkedRoleData[j].id === v.tableData[i].id) {
                            v.checkedRoleData.splice(j, 1);
                            break;
                        }
                    }
                }
            }
            if (v.checkedRoleData.length === 0) {
                for (let i = 0; i < val.length; i++) {
                    v.checkedRoleData.push(val[i]);
                }
            } else {
                for (let i = 0; i < val.length; i++) {
                    let flag = false;
                    for (let j = 0; j < v.checkedRoleData.length; j++) {
                        if (v.checkedRoleData[j].id === val[i].id) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag)
                        v.checkedRoleData.push(val[i]);
                }
            }
        },
        handleSelect(val, row) {
            /* 1 => add ; 0 => remove*/
            let flag = 0;
            for (let i = 0; i < val.length; i++) {
                if (row.id === val[i].id) {
                    flag = 1;
                    break;
                }
            }
            if (flag === 1) {
                this.checkedRoleData.push(row);
            } else {
                for (let i = 0; i < this.checkedRoleData.length; i++) {
                    if (this.checkedRoleData[i].id === row.id)
                        this.checkedRoleData.splice(i, 1);
                }
            }
        },
        save() {
            let vm = this;
            let roleIds = this.checkedRoleData.map(item => {
                return item.id
            });
            http.post(BD.realPath + '/permission/account/bindRoles', {
                accountId: this.accountId,
                roleIds: roleIds.join(",")
            }).then((response) => {
                const {rtnFlag, rtnMsg} = response;
                vm.$message.success(rtnMsg);
                vm.close();
            }).catch(function (error) {
                console.log(error);
            });
        },
        close() {
            this.accountId = '';
            this.tableData = [];
            this.checkedRoleData = [];
            this.formShow = false;
        },
        query() {
            let vm = this;
            vm.loading = true;
            Promise.all([loadRoles(), loadLinkRolesByAccId(vm.accountId)]).then(res => {
                vm.tableData = res[0];
                let roleIds = res[1].map(item => {
                    return item
                });
                let links = res[0].filter(item => {
                    return roleIds.indexOf(item.id) > -1
                });
                vm.hasCheckedData = links;
                links.forEach(function (row) {
                    vm.tableData.forEach(function (a) {
                        if (a.id === row.id) {
                            vm.$nextTick(function () {
                                vm.$refs.datagrid.toggleRowSelection(a);
                            });
                        }
                    })
                });
                vm.loading = false;
            })
        }
    },
    mounted() {
    }
});

function loadRoles() {
    return new Promise((resolve, reject) => {
        http.post(BD.realPath + '/permission/role/loadRoles', {status: 'enabled'}).then((response) => {
            const {rtnData} = response;
            resolve(rtnData || []);
        }).catch(function (error) {
            resolve(error);
        });
    })
}

function loadLinkRolesByAccId(accountId) {
    return new Promise((resolve, reject) => {
        http.post(BD.realPath + '/permission/account/loadLinkRolesByAccId', {accountId: accountId}).then((response) => {
            const {rtnData} = response;
            if (rtnData && rtnData.length > 0) {
                let links = rtnData.map(item => {
                    return item.roleId
                });
                resolve(links);
            }
            resolve([]);
        }).catch(function (error) {
            reject(error);
        });
    })
}

