let main = new Vue({
    el: "#main",
    // router: router,
    data: {
        isCollapse: false,
        menus: [],
        currentTabName: 'home',
        editableTabs: [],
        coll: '',
        breadcrumb: [],
        // 用来存储已经打开过的面包屑路径
        rememberBreadCrumb: {home: ['首页']},
        homeActiveIndex: '0',
        tableData: [],
        homeTableHeight: null,
        form: {
            oldPassword: '',
            password: '',
            ensurePassword: ''
        },
        rules: {
            oldPassword: [
                {required: true, message: '该项不能为空', trigger: 'blur'}
            ],
            password: [
                {required: true, message: '该项不能为空', trigger: 'blur'}
            ],
            ensurePassword: [
                {required: true, message: '该项不能为空', trigger: 'blur'},
                {
                    validator: (rule, value, callback) => {
                        if (value !== '' && value !== main.form.password) {
                            callback(new Error('当前两次输入密码不相同,请重新输入'));
                        } else {
                            callback();
                        }
                    }, trigger: 'blur'
                }
            ]
        }
    },
    methods: {
        change() {
            this.isCollapse = !this.isCollapse;
            if (this.isCollapse) {
                this.coll = "el-menu--collapse"
            } else {
                this.coll = ''
            }
        },
        tabClick(tab) {
            this.breadcrumb = this.rememberBreadCrumb[tab.name]
        },
        handleSelect(index, indexPath, menu) {
            let arr = index.split('/');
            let name = arr[arr.length - 1];
            let exist = this.editableTabs.some(tab => {
                return tab.name === name;
            });
            let nodes = menu.$el.childNodes;
            let title = '';
            if (nodes && nodes.length === 3 && nodes[2].innerText) {
                title = nodes[2].innerText;
            }
            if (!exist) {
                // axios.get(`/loadMainDiv?page=${index}`).then(response => {
                this.editableTabs.push({
                    label: title,
                    name: name,
                    url: BD.realPath + index
                });
                setTimeout(() => {
                    let arr = document.getElementsByClassName('el-tabs__item');
                    for (let dom of arr) {
                        dom.oncontextmenu = function (e) {
                            e.preventDefault();
                        };
                        dom.addEventListener("dblclick", (e) => {
                            let id = "pane-" + e.currentTarget.id.split("-")[1];
                            let iframe = document.getElementById(id).firstElementChild;
                            let src = iframe.getAttribute("src");
                            iframe.setAttribute("src", src);
                        });
                    }
                }, 800);
            }
            indexPath[indexPath.length - 1] = title;
            this.breadcrumb = indexPath;
            this.currentTabName = name;
            this.rememberBreadCrumb[name] = this.breadcrumb;
        },
        removeTab(targetName) {
            let tabs = this.editableTabs;
            let activeName = this.currentTabName;
            if (activeName === targetName) {
                tabs.forEach((tab, index) => {
                    if (tab.name === targetName) {
                        let nextTab = tabs[index + 1] || tabs[index - 1];
                        if (nextTab) {
                            activeName = nextTab.name;
                        }
                    }
                });
            }
            delete this.rememberBreadCrumb[targetName];
            this.currentTabName = activeName;
            this.editableTabs = tabs.filter(tab => tab.name !== targetName);
            if (tabs.length === 1) {
                this.currentTabName = 'home'
            }
            if (this.rememberBreadCrumb[this.currentTabName]) {
                this.breadcrumb = this.rememberBreadCrumb[this.currentTabName]
            } else {
                this.breadcrumb = []
            }

        },
        handleHomeSelect(index, indexPath, menu) {
            this.homeActiveIndex = index;
        },
        updatePwd(formName) {
            this.$refs[formName].validate((valid, fields) => {
                if (valid) {
                    http.post(BD.realPath + '/permission/account/updatePwd1', main.form).then(response => {
                        const {rtnMsg} = response;
                        drawer.$message.success(rtnMsg || "操作成功");
                        drawer.show = false;
                    })
                } else {
                    console.log(fields);
                    return false;
                }
            });
        }
    },
    watch: {
        'homeActiveIndex': val => {
            main.form = {
                oldPassword: '',
                password: '',
                ensurePassword: ''
            };
            if (val === '1') {
                http.post(BD.realPath + '/loadLoginLog').then(response => {
                    const {rtnData} = response;
                    main.tableData = rtnData;
                });
            }
        }
    },
    mounted() {
        let vm = this;
        http.post(BD.realPath + '/loadMenus').then(response => {
            const {rtnData} = response;
            this.menus = rtnData;
        });
        this.handleHomeSelect('1');
        vm.homeTableHeight = document.documentElement.clientHeight - 200;
        window.onresize = function temp() {
            vm.homeTableHeight = document.documentElement.clientHeight - 200;
        };
    }
});