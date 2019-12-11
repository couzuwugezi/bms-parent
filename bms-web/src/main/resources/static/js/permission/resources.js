let queryBlock = new Vue({
    el: "#queryBlock",
    data: {
        searchForm: {
            // currentPage: 1,
            // pageSize: 10,
            name: '',
            type: '',
            status: ''
        }
    },
    methods: {
        query() {
            datagrid.query(this.searchForm);
        },
        add(type, row) {
            dialog.title = '新增资源';
            dialog.resForm = {};
            datagrid.currentRow = {};
            dialog.visible = true
        }
    }
});

let datagrid = new Vue({
    el: "#datagrid",
    data: {
        height: null,
        tableData: [],
        loading: false,
        currentRow: {}
    },
    methods: {
        changeResStatus(row, status) {
            let vm = this;
            http.post(`/permission/resource/changeResStatus/${row.id}/${status}`).then(function (response) {
                const {rtnMsg} = response;
                datagrid.$message.success(rtnMsg || '操作成功！');
                queryBlock.query()
            }).catch(function (error) {
                console.log(error)
            })
        },
        query(param) {
            this.loading = true;
            http.post('/permission/resource/loadRes', param).then(function (response) {
                const {rtnData} = response;
                datagrid.loading = false;
                datagrid.tableData = rtnData;
            }).catch(function (error) {
                console.log(error)
            })
        },
        add(type, row) {
            let data = JSON.parse(JSON.stringify(row));
            if (type === 'addSub') {
                dialog.title = '新增子资源';
                datagrid.currentRow = data
            } else if (type === 'edit') {
                dialog.title = '修改资源';
                dialog.resForm = {
                    id: data.id,
                    parentId: data.parentId,
                    type: data.type,
                    name: data.name,
                    url: data.url,
                    sort: data.sort
                }
            }
            dialog.visible = true
        }
    },
    mounted: function () {
        let vm = this;
        this.query(queryBlock.searchForm);
        let queryBlockHright = document.getElementById("queryBlock").scrollHeight;
        let headerHeight = queryBlockHright + 32;
        vm.height = document.documentElement.clientHeight - headerHeight;
        window.onresize = function temp() {
            vm.height = document.documentElement.clientHeight - headerHeight;
        };
    }
});

let dialog = new Vue({
    el: "#dialog",
    data: {
        visible: false,
        resForm: {
            id: '',
            parentId: '',
            type: '',
            name: '',
            url: '',
            sort: ''
        },
        title: '',
        resFormRules: {
            type: [
                {required: true, message: '选择资源类型', trigger: 'blur'}
            ],
            name: [
                {required: true, message: '请输入资源名称', trigger: 'blur'}
            ],
            sort: [
                {required: true, message: '请输入排序号', trigger: 'blur'}
            ]
        }
    },
    methods: {
        save() {
            let vm = this;
            this.$refs['resForm'].validate((valid) => {
                if (valid) {
                    let url = null;
                    if (vm.title === '新增资源') {
                        url = '/permission/resource/add';
                        vm.resForm.parentId = 0
                    } else if (vm.title === '新增子资源') {
                        url = '/permission/resource/add';
                        vm.resForm.parentId = datagrid.currentRow.id
                    } else {
                        url = '/permission/resource/editRes'
                    }
                    let form = JSON.parse(JSON.stringify(vm.resForm));
                    http.post(url, form)
                        .then(function (response) {
                            dialog.$message.success(vm.title + '成功！');
                            vm.close()
                        })
                } else {
                    console.log('error submit!!');
                    return false
                }
            })
        },
        close() {
            this.resForm = {
                id: '',
                parentId: '',
                type: '',
                name: '',
                filePath: '',
                sort: '',
                status: ''
            };
            this.title = '';
            this.visible = false;
            queryBlock.query()
        },
    }
});