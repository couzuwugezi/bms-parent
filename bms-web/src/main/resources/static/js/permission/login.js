let vm = new Vue({
    el: "#form",
    data: {
        errMsg: '',
        show: false
    }
});

let drawer = new Vue({
    el: "#drawer",
    data: {
        show: false,
        form: {
            loginName: '',
            password: '',
            oldPassword: '',
            ensurePassword: ''
        },
        rules: {
            password: [
                {required: true, message: '该项不能为空', trigger: 'blur'},
                {
                    validator: (rule, value, callback) => {
                        if (value !== '' && value === drawer.form.oldPassword) {
                            callback(new Error('当前密码与原始密码相同,请核实'));
                        } else if (drawer.form.ensurePassword !== '' && value !== drawer.form.ensurePassword) {
                            callback(new Error('当前两次输入密码不相同,请核实'));
                        } else {
                            callback();
                        }
                    }, trigger: 'blur'
                }
            ],
            ensurePassword: [
                {required: true, message: '该项不能为空', trigger: 'blur'},
                {
                    validator: (rule, value, callback) => {
                        if (value !== '' && value !== drawer.form.password) {
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
        updatePwd(form) {
            this.$refs[form].validate((valid, fileds) => {
                if (valid) {
                    http.post('/permission/account/updatePwd', drawer.form).then(response => {
                        const {rtnMsg} = response;
                        drawer.$message.success(rtnMsg || "操作成功");
                        drawer.show = false;
                    })
                } else {
                    console.log(fileds)
                }
            });
        }
    }
});

function changePwd() {
    drawer.show = true;
    drawer.form = {
        loginName: '',
        password: '',
        ensurePassword: ''
    }
}

document.onkeydown = function (event) {
    if (event.key === 'Enter') {
        submitForm();
    }
};

function changeBackImg(num) {
    let imgArr = ['bg1.jpg', 'bg3.jpg', 'bg4.jpg', 'bg5.jpg', 'bg6.jpg', 'bg7.jpg', 'bg8.jpg', 'bg11.jpg', 'header.jpg', 'login.jpg'];
    let backImg = 'assets/img/' + imgArr[num];
    document.getElementsByClassName("page-header-image")[0].style.backgroundImage = 'url(' + backImg + ')';
}

function submitForm() {
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    if (username === "" || password === "") {
        vm.errMsg = "请输入用户名或密码";
        vm.show = true;
        return false;
    } else {
        vm.show = false;
        axios.post(BD.localhostPath + "/check", {
            username: username,
            password: password,
            captchaCode: document.getElementById("captcha") ? document.getElementById("captcha").value : null
        }).then(response => {
            const {rtnFlag, rtnMsg} = response.data;
            if (rtnFlag !== '9999') {
                vm.errMsg = rtnMsg || "系统异常,请联系管理员";
                document.getElementById("captchaImg").click();
                vm.show = true;
                if (rtnFlag === '1002') {
                    drawer.show = true;
                    drawer.form = {
                        loginName: username,
                        oldPassword: password,
                        password: '',
                        ensurePassword: ''
                    }
                }
            } else {
                parent.location.href = "/index.html"
            }
        });
    }
}