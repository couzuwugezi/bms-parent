let role = new Vue({
    el: "#role",
    data: {
        treeData: [],
        checkedData: [],
        defaultProps: {
            children: 'children',
            label: 'name'
        },
        height: null,
        queryForm: {
            name: '',
            description: ''
        },
        statusFormat(a, b, c) {
            if (c === 'enabled') {
                return "启用"
            } else if (c === 'disable') {
                return "禁用"
            }
        },
        radio: '',
        tableData: [],
        loading: false,
        treeLoading: false,
        chooseData: {},
        options: {},
        permissionArr: []
    },
    methods: {
        edit(row) {
            let data = JSON.parse(JSON.stringify(row));
            dialog.form = {
                id: data.id,
                name: data.name,
                description: data.description
            };
            dialog.title = "编辑";
            dialog.visible = true;
        },
        loadAuth(roleId) {
            http.post('/permission/dict/loadDicts/1').then(response => {
                const {rtnData} = response;
                this.options = rtnData;

                http.post(`/permission/dict/loadAuth/${roleId}`).then(response => {
                    const {rtnData} = response;
                    this.permissionArr = rtnData
                })
            })
        },
        auth(roleId) {
            http.post(`/permission/role/auth/${roleId}`, {
                auths: this.permissionArr.join(",")
            }).then(response => {
                const {rtnMsg} = response;
                this.$message.success(rtnMsg);
                this.permissionArr = [];
            })
        },
        query() {
            let vm = this;
            vm.loading = true;
            http.post('/permission/role/loadRoles', this.queryForm).then((response) => {
                const {rows, total, rtnData} = response;
                vm.tableData = rtnData;
                vm.loading = false;
                vm.chooseData = {};
            }).catch(function (error) {
                console.log(error)
            })
        },
        add() {
            dialog.form = {
                id: '',
                name: '',
                description: ''
            };
            dialog.title = "新增";
            dialog.visible = true;
        },
        handleSelect: function (row) {
            this.chooseData = row;
        },
        updateStatus(status) {
            let id = this.chooseData.id;
            if (!id) {
                this.$message.warning("请选择一个角色");
                return false;
            }
            let vm = this;
            http.post("/permission/role/updateStatus", {id: this.chooseData.id, status: status}).then(response => {
                const {rtnMsg} = response;
                vm.$message.success({
                    message: rtnMsg || "操作成功"
                });
                vm.query();
            })
        },
        bindRes() {
            let vm = this;
            let id = this.chooseData.id;
            if (!id) {
                this.$message.warning("请选择一个角色");
                return false;
            }
            let keys = vm.$refs.tree.getCheckedKeys(true);
            // let nodes = vm.$refs.tree.getCheckedNodes(false, true);
            // let keys = nodes.map(item => {
            //     return item.id
            // });
            http.post("/permission/role/bindRes", {id: this.chooseData.id, keys: keys.join(",")}).then(response => {
                const {rtnMsg} = response;
                vm.$message.success({
                    message: rtnMsg || "操作成功"
                });
                vm.query();
            })
        },
        reloadRes() {
            this.treeLoading = true;
            http.post('/permission/resource/loadEnabledResource').then(function (response) {
                const {rtnData} = response;
                role.treeLoading = false;
                role.treeData = rtnData;
            }).catch(function (error) {
                console.log(error)
            });
        }
    },
    watch: {
        'chooseData': val => {
            if (val.id) {
                http.post("/permission/role/loadResByRoleId", {roleId: val.id}).then(response => {
                    const {rtnData} = response;
                    if (rtnData) {
                        role.$refs.tree.setCheckedKeys(rtnData)
                    } else {
                        role.$refs.tree.setCheckedKeys([])
                    }
                })
            } else {
                role.radio = '';
                role.$refs.tree.setCheckedKeys([])
            }
        }
    },
    mounted() {
        let vm = this;
        vm.reloadRes();
        let queryBlockHright = document.getElementById("queryForm").scrollHeight;
        let headerHeight = queryBlockHright + 60;
        vm.height = document.documentElement.clientHeight - headerHeight;
        window.onresize = function temp() {
            vm.height = document.documentElement.clientHeight - headerHeight;
        };
        vm.query();
    }
});

let dialog = new Vue({
    el: "#dialog",
    data: {
        title: '',
        visible: false,
        form: {
            id: '',
            name: '',
            description: ''
        },
        rules: {
            name: [
                {required: true, message: '此项不能为空', trigger: "blur"},
                {
                    validator: (rule, value, callback) => {
                        if (value && /^[a-zA-Z]+$/.test(value)) {
                            callback();
                        } else {
                            callback(new Error('只能输入字母'));
                        }
                    }, trigger: 'blur'
                }
            ]
        }
    },
    methods: {
        save(formName) {
            let vm = this;
            this.$refs[formName].validate((valid, fields) => {
                if (valid) {
                    http.post('/permission/role/add', vm.form).then(response => {
                        const {rtnFlag, rtnMsg} = response;
                        vm.$message.success(rtnMsg);
                        role.query();
                        dialog.close();
                    })
                } else {
                    console.log(fields);
                    return false;
                }
            });
        },
        close() {
            this.form = {
                id: '',
                name: '',
                description: ''
            };
            this.visible = false;
        }
    }
});