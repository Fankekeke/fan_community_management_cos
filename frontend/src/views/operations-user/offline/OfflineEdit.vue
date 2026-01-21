<template>
  <a-modal v-model="show" title="修改线下活动" @cancel="onClose" :width="800">
    <template slot="footer">
      <a-button key="back" @click="onClose">
        取消
      </a-button>
      <a-button key="submit" type="primary" :loading="loading" @click="handleSubmit">
        修改
      </a-button>
    </template>
    <a-form :form="form" layout="vertical">
      <a-row :gutter="20">
        <a-col :span="12">
          <a-form-item label='活动名称' v-bind="formItemLayout">
            <a-input v-decorator="[
      'title',
      { rules: [{ required: true, message: '请输入活动名称!' }] }
      ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='活动地点' v-bind="formItemLayout">
            <a-input-search
              v-decorator="[
              'location'
              ]"
              enter-button="选择"
              @search="showChildrenDrawer"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='经度' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'longitude',
      { rules: [{ required: true, message: '请输入经度!' }] }
      ]" style="width: 100%" :precision="6"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='纬度' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'latitude',
      { rules: [{ required: true, message: '请输入纬度!' }] }
      ]" style="width: 100%" :precision="6"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='门票价格' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'ticketPrice',
      { rules: [{ required: true, message: '请输入门票价格!' }] }
      ]" style="width: 100%" :min="0" :step="0.01"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='总名额' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'totalCapacity',
      { rules: [{ required: true, message: '请输入总名额!' }] }
      ]" style="width: 100%" :min="1"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='剩余名额' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'remainingCapacity',
      { rules: [{ required: true, message: '请输入剩余名额!' }] }
      ]" style="width: 100%" :min="0"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='活动日期' v-bind="formItemLayout">
            <a-date-picker v-decorator="[
      'eventDate',
      { rules: [{ required: true, message: '请选择活动日期!' }] }
      ]" style="width: 100%"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='活动详情介绍' v-bind="formItemLayout">
            <a-textarea :rows="4" v-decorator="[
      'description',
       { rules: [{ required: true, message: '请输入活动详情介绍!' }] }
      ]"/>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script>
import baiduMap from '@/utils/map/baiduMap'
import drawerMap from '@/utils/map/searchmap/drawerMap'
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}
const formItemLayout = {
  labelCol: { span: 24 },
  wrapperCol: { span: 24 }
}
export default {
  name: 'BulletinEdit',
  props: {
    bulletinEditVisiable: {
      default: false
    }
  },
  components: {
    drawerMap
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.bulletinEditVisiable
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      rowId: null,
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      localPoint: {},
      staymerchant: '',
      childrenDrawer: false
    }
  },
  methods: {
    handlerClosed (localPoint) {
      this.childrenDrawer = false
      if (localPoint !== null && localPoint !== undefined) {
        this.localPoint = localPoint
        console.log(this.localPoint)

        let address = baiduMap.getAddress(localPoint)
        address.getLocation(localPoint, (rs) => {
          if (rs != null) {
            if (rs.address !== undefined && rs.address.length !== 0) {
              this.stayAddress = rs.address
            }
            if (rs.surroundingPois !== undefined) {
              if (rs.surroundingPois.address !== undefined && rs.surroundingPois.address.length !== 0) {
                this.stayAddress = rs.surroundingPois.address
              }
            }
            let obj = {}
            obj['location'] = this.stayAddress
            obj['longitude'] = localPoint.lng
            obj['latitude'] = localPoint.lat
            this.form.setFieldsValue(obj)
          }
        })
      }
    },
    showChildrenDrawer (value) {
      this.flagType = value
      this.childrenDrawer = true
    },
    onChildrenDrawerClos () {
      this.childrenDrawer = false
    },
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({ fileList }) {
      this.fileList = fileList
    },
    imagesInit (images) {
      if (images !== null && images !== '') {
        let imageList = []
        images.split(',').forEach((image, index) => {
          imageList.push({uid: index, name: image, status: 'done', url: 'http://127.0.0.1:9527/imagesWeb/' + image})
        })
        this.fileList = imageList
      }
    },
    setFormValues ({...bulletin}) {
      this.rowId = bulletin.id
      let fields = ['title', 'location', 'longitude', 'latitude', 'ticketPrice', 'totalCapacity', 'remainingCapacity', 'eventDate', 'description']
      let obj = {}
      Object.keys(bulletin).forEach((key) => {
        if (key === 'eventDate' && bulletin[key] != null) {
          bulletin[key] = moment(bulletin[key])
        }
        if (key === 'images') {
          this.fileList = []
          this.imagesInit(bulletin['images'])
        }
        if (fields.indexOf(key) !== -1) {
          this.form.getFieldDecorator(key)
          obj[key] = bulletin[key]
        }
      })
      this.form.setFieldsValue(obj)
    },
    reset () {
      this.loading = false
      this.form.resetFields()
    },
    onClose () {
      this.reset()
      this.$emit('close')
    },
    handleSubmit () {
      // 获取图片List
      let images = []
      this.fileList.forEach(image => {
        if (image.response !== undefined) {
          images.push(image.response)
        } else {
          images.push(image.name)
        }
      })
      this.form.validateFields((err, values) => {
        values.id = this.rowId
        values.images = images.length > 0 ? images.join(',') : null
        values.eventDate = moment(values.eventDate).format('YYYY-MM-DD')
        if (!err) {
          this.loading = true
          this.$put('/cos/offline-events', {
            ...values
          }).then((r) => {
            this.reset()
            this.$emit('success')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
