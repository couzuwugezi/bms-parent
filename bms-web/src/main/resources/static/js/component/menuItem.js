Vue.component('menu-item', {
    template: `<div :class="coll">
                    <el-submenu v-if="menus.children && menus.children.length > 0" class="aside-container__title" :index="menus.name"> 
                        <template slot="title"> 
                            <i class="el-icon-menu"></i> 
                                <span>{{menus.name}}</span> 
                        </template> 
                        <menu-item v-for="menu in menus.children" :menus="menu" :key="menu.name"></menu-item> 
                    </el-submenu> 
                    <div class="aside-container__title2" v-else> 
                        <el-menu-item class="aside-container__option1" :index="menus.url"> 
                            <i class="el-icon-location"></i> 
                            <span slot="title">{{menus.name}}</span> 
                        </el-menu-item>
                    </div>
                </div>`,
    props: {
        menus: {
            type: [Array, Object],
            default: () => {
            },
            required: true
        },
        coll: {
            type: String
        }
    },
    data() {
        return {}
    },
    methods: {},
    mounted() {
    }
});