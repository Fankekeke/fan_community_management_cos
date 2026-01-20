<template>
  <a-modal v-model="show" title="新增周边商品" @cancel="onClose" :width="800">
    <template slot="footer">
      <a-button key="back" @click="onClose">
        取消
      </a-button>
      <a-button key="submit" type="primary" :loading="loading" @click="handleSubmit">
        提交
      </a-button>
    </template>
    <a-form :form="form" layout="vertical">
      <a-row :gutter="20">
        <a-col :span="12">
          <a-form-item label='商品名称' v-bind="formItemLayout">
            <a-input v-decorator="[
      'name',
      { rules: [{ required: true, message: '请输入商品名称!' }] }
      ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='分类' v-bind="formItemLayout">
            <a-select v-decorator="[
      'category',
      { rules: [{ required: true, message: '请选择分类!' }] }
      ]">
              <a-select-option value="周边">周边</a-select-option>
              <a-select-option value="专辑">专辑</a-select-option>
              <a-select-option value="衣服">衣服</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='售价' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'price',
      { rules: [{ required: true, message: '请输入售价!' }] }
      ]" style="width: 100%" :min="0" :step="0.01"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='库存' v-bind="formItemLayout">
            <a-input-number v-decorator="[
      'stock',
      { rules: [{ required: true, message: '请输入库存!' }] }
      ]" style="width: 100%" :min="0"/>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label='是否在售' v-bind="formItemLayout">
            <a-select v-decorator="[
      'isOnSale',
      { rules: [{ required: true, message: '请选择是否在售!' }] }
      ]">
              <a-select-option value="0">是</a-select-option>
              <a-select-option value="1">否</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='商品描述' v-bind="formItemLayout">
            <a-textarea :rows="4" v-decorator="[
      'description',
       { rules: [{ required: true, message: '请输入商品描述!' }] }
      ]"/>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='图册' v-bind="formItemLayout">
            <a-upload
              name="avatar"
              action="http://127.0.0.1:9527/file/fileUpload/"
              list-type="picture-card"
              :file-list="fileList"
              @preview="handlePreview"
              @change="picHandleChange"
            >
              <div v-if="fileList.length < 8">
                <a-icon type="plus" />
                <div class="ant-upload-text">
                  Upload
                </div>
              </div>
            </a-upload>
            <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
              <img alt="example" style="width: 100%" :src="previewImage" />
            </a-modal>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script>
import {mapState} from 'vuex'
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
  name: 'BulletinAdd',
  props: {
    bulletinAddVisiable: {
      default: false
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    show: {
      get: function () {
        return this.bulletinAddVisiable
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: ''
    }
  },
  methods: {
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
        images.push(image.response)
      })
      this.form.validateFields((err, values) => {
        values.imageUrl = images.length > 0 ? images.join(',') : null
        if (!err) {
          values.publisher = this.currentUser.userId
          this.loading = true
          this.$post('/cos/merchandise', {
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
