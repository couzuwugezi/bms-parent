let table = new Vue({
    el: "#table",
    data: {
        tableData: [],
        height: null,
        loading: false
    },
    methods: {
        query() {
            http.post('/permission/dict/loadDicts/1').then(response => {
                const {rtnData} = response;
                this.tableData = rtnData
            })
        },
        edit(row) {
            addForm.form = {
                id: row.id,
                label: row.label,
                value: row.value,
                detail: row.detail
            };
            addForm.btnText = "修改权限"
        }
    },
    mounted() {
        this.query();
        let vm = this;
        let addFormHeight = document.getElementById("addForm").scrollHeight;
        let headerHeight = addFormHeight + 90;
        vm.height = document.documentElement.clientHeight - headerHeight;
        window.onresize = function temp() {
            vm.height = document.documentElement.clientHeight - headerHeight;
        };
    }
});

let addForm = new Vue({
    el: "#addForm",
    data: {
        rules: {
            label: [
                {required: true, message: '此项不能为空', trigger: "blur"}
            ],
            value: [
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
        },
        btnText: '新增权限',
        form: {
            id: '',
            label: '',
            value: '',
            detail: ''
        }
    },
    methods: {
        add() {
            this.$refs['addForm'].validate((valid, fields) => {
                if (valid) {
                    http.post('/permission/dict/addDicts/1', this.form).then(response => {
                        const {rtnMsg} = response;
                        this.$message.success(rtnMsg);
                        this.form = {
                            id: '',
                            label: '',
                            value: '',
                            detail: ''
                        };
                        if (this.btnText === '新增权限') {
                            this.btnText = '修改权限'
                        } else {
                            this.btnText = '新增权限'
                        }
                        table.query();
                    })
                } else {
                    console.log(fields);
                    return false;
                }
            });
        }
    }
});