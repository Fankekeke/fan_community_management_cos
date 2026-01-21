
<template>
  <div class="merchandise-container" style="width: 100%">
    <a-card :bordered="false" class="card-area">
      <div class="goods-grid">
        <a-card
          v-for="item in goodsList"
          :key="item.id"
          class="goods-card"
          hoverable
        >
          <a href="javascript:void(0)" slot="cover" @click="viewDetail(item)">
            <img
              :src="getFirstImageUrl(item.imageUrl)"
              class="goods-image"
              :class="{ disabled: !canPurchase(item) }"
              alt="商品图片"
            />
            <!-- 遮罩层显示商品不可售状态 -->
            <div
              v-if="!canPurchase(item)"
              class="sold-out-overlay"
            >
              <div class="sold-out-text">{{ item.isOnSale === '1' ? '已下架' : '已售罄' }}</div>
            </div>
          </a>

          <a-card-meta :title="item.name" class="goods-meta">
            <template slot="description">
              <div class="goods-price">
                <span v-if="memberInfo" class="original-price">¥{{ item.price.toFixed(2) }}</span>
                <span>¥{{ getDiscountedPrice(item.price).toFixed(2) }}</span>
              </div>
              <div class="goods-info">
                <span class="goods-category">{{ item.category }}</span>
                <span class="goods-stock" :class="{ low: item.stock <= 10, out: item.stock === 0 }">
                  {{ item.stock === 0 ? '已售罄' : `库存${item.stock}` }}
                </span>
              </div>
              <div class="goods-description">{{ item.description }}</div>
              <!-- 显示商品状态标签 -->
              <div class="goods-status-tags">
                <a-tag v-if="memberInfo" color="blue" class="status-tag">会员专享</a-tag>
                <a-tag v-if="item.isOnSale === '1'" color="red" class="status-tag">已下架</a-tag>
                <a-tag v-if="item.stock === 0" color="orange" class="status-tag">缺货</a-tag>
              </div>
            </template>
          </a-card-meta>

          <template class="ant-card-actions" slot="actions">
            <div style="padding: 0 15px">
              <!-- 根据库存和销售状态判断是否显示购买按钮 -->
              <a-button
                type="primary"
                @click="showPurchaseModal(item)"
                :disabled="!canPurchase(item)"
                class="buy-btn"
              >
                {{ canPurchase(item) ? '立即购买' : '已售罄' }}
              </a-button>
            </div>
          </template>
        </a-card>
      </div>
    </a-card>

    <!-- 商品购买弹窗 -->
    <a-modal
      v-model="purchaseModalVisible"
      :title="currentItem ? currentItem.name : '商品详情'"
      @ok="confirmPurchase"
      @cancel="hidePurchaseModal"
      :width="500"
      okText="确认购买"
      cancelText="取消"
    >
      <div v-if="currentItem" class="purchase-modal-content">
        <div class="product-summary">
          <img :src="getFirstImageUrl(currentItem.imageUrl)" class="modal-product-image" alt="商品图片" />
          <div class="product-info">
            <h3>{{ currentItem.name }}</h3>
            <div class="modal-price">
              <span v-if="memberInfo" class="original-price">¥{{ currentItem.price.toFixed(2) }}</span>
              <span>¥{{ getDiscountedPrice(currentItem.price).toFixed(2) }}</span>
            </div>
            <div class="modal-stock">库存: {{ currentItem.stock }}</div>
            <div v-if="memberInfo" class="member-discount">会员专享 8折优惠</div>
          </div>
        </div>

        <!-- 图片轮播区域 -->
        <div class="image-carousel" v-if="getImageUrls(currentItem.imageUrl).length > 1">
          <a-carousel arrows dotsClass="slick-dots slick-thumb">
            <div v-for="(url, index) in getImageUrls(currentItem.imageUrl)" :key="index">
              <img :src="'http://127.0.0.1:9527/imagesWeb/' + url" alt="商品图片" />
            </div>
          </a-carousel>
        </div>

        <a-form layout="vertical" :form="form">
          <a-form-item label="购买数量">
            <a-input-number
              v-decorator="['quantity', { initialValue: 1, rules: [{ required: true, message: '请输入购买数量' }] }]"
              :min="1"
              :max="currentItem.stock"              style="width: 100%"
              @change="handleQuantityChange"
            />
            <div class="quantity-tip">最多可购买 {{ currentItem.stock }} 件</div>
          </a-form-item>

          <a-form-item label="收货地址">
            <a-input
              v-decorator="['deliveryAddress', { rules: [{ required: true, message: '请输入收货地址' }] }]"
              placeholder="请输入收货地址"
              type="textarea"
              :rows="3"
            />
          </a-form-item>
        </a-form>

        <div class="total-price">
          <div class="original-total" v-if="memberInfo">
            原价总计: ¥{{ (currentItem.price * quantity).toFixed(2) }}
          </div>
          <div>
            优惠后总计: ¥{{ totalPrice.toFixed(2) }}
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>import RangeDate from '@/components/datetime/RangeDate'
import BulletinAdd from './MerchandiseAdd.vue'
import BulletinEdit from './MerchandiseEdit.vue'
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'Merchandise',
  components: {BulletinAdd, BulletinEdit, RangeDate},
  data () {
    return {
      goodsList: [],
      purchaseModalVisible: false,
      currentItem: null,
      memberInfo: null,
      quantity: 1,
      form: this.$form.createForm(this)
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    totalPrice () {
      if (this.currentItem) {
        return this.getDiscountedPrice(this.currentItem.price) * this.quantity
      }
      return 0
    }
  },
  mounted () {
    this.queryAllList()
    this.selectMemberByUserId()
  },
  methods: {
    selectMemberByUserId () {
      this.$get(`/cos/member-order-info/member/${this.currentUser.userId}`).then((r) => {
        this.memberInfo = r.data.member
      })
    },
    queryAllList () {
      this.$get('/cos/merchandise/list').then((r) => {
        this.goodsList = r.data.data
      })
    },
    // 获取折扣后的价格
    getDiscountedPrice (price) {
      if (this.memberInfo) {
        return price * 0.8 // 8折优惠
      }
      return price
    },
    // 获取图片URL数组
    getImageUrls (imageUrl) {
      if (!imageUrl) return []
      return imageUrl.split(',').filter(url => url.trim() !== '')
    },
    // 获取第一张图片URL
    getFirstImageUrl (imageUrl) {
      const urls = this.getImageUrls(imageUrl)
      return urls.length > 0 ? 'http://127.0.0.1:9527/imagesWeb/' + urls[0] : ''
    },
    // 判断商品是否可以购买
    canPurchase (item) {
      return item.stock > 0 && item.isOnSale === '0'
    },
    // 显示购买弹窗
    showPurchaseModal (item) {
      this.currentItem = item
      this.purchaseModalVisible = true

      // 设置表单初始值
      this.$nextTick(() => {
        this.form.setFieldsValue({
          quantity: 1,
          deliveryAddress: ''
        })
      })
    },
    // 隐藏购买弹窗
    hidePurchaseModal () {
      this.purchaseModalVisible = false
      this.currentItem = null
    },
    // 确认购买
    confirmPurchase () {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.$message.success(`成功下单 ${values.quantity} 件 ${this.currentItem.name}，会员享受8折优惠`)
          const purchaseData = {
            productId: this.currentItem.id,
            productName: this.currentItem.name,
            quantity: values.quantity,
            price: this.getDiscountedPrice(this.currentItem.price), // 使用折扣价
            originalPrice: this.currentItem.price, // 原价
            totalAmount: this.totalPrice,
            deliveryAddress: values.deliveryAddress,
            isMember: !!this.memberInfo // 是否会员
          }

          let data = {totalAmount: this.totalPrice, merchandiseId: this.currentItem.id, quantity: values.quantity, userId: this.currentUser.userId, shippingAddress: values.deliveryAddress}
          this.$post('/cos/pay/merchandise', data).then((r) => {
            // 添加之前先删除一下，如果单页面，页面不刷新，添加进去的内容会一直保留在页面中，二次调用form表单会出错
            const divForm = document.getElementsByTagName('div')
            if (divForm.length) {
              document.body.removeChild(divForm[0])
            }
            const div = document.createElement('div')
            div.innerHTML = r.data.msg // data就是接口返回的form 表单字符串
            // console.log(div.innerHTML)
            document.body.appendChild(div)
            document.forms[0].setAttribute('target', '_self') // 新开窗口跳转
            document.forms[0].submit()
          })
          console.log('购买数据:', purchaseData)
          this.hidePurchaseModal()
        }
      })
    },
    // 数量变化处理
    handleQuantityChange (value) {
      this.quantity = value
    },
    buyNow (item) {
      if (this.canPurchase(item)) {
        console.log('购买商品:', item)
        // 这里可以跳转到购买页面或执行购买逻辑
      } else {
        this.$message.warning('该商品暂时无法购买')
      }
    },
    viewDetail (item) {
      if (this.canPurchase(item)) {
        console.log('查看商品详情:', item)
        // 这里可以跳转到商品详情页
      } else {
        this.$message.info('该商品暂时无法查看')
      }
    }
  },
  watch: {}
}
</script>

<style lang="less" scoped>@import "../../../../static/less/Common";

.merchandise-container {
  padding: 20px;
}

.card-area {
  background: #fff;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  padding: 10px;
}

.goods-card {
  width: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: visible;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  }

  // 不可购买时的样式
  &.disabled {
    opacity: 0.7;
    filter: grayscale(30%);
  }
}

.goods-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: all 0.3s ease;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;

  &.disabled {
    opacity: 0.6;
  }

  &:hover {
    transform: scale(1.03);
  }
}

.sold-out-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 200px;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px 8px 0 0;
  z-index: 10;
}

.sold-out-text {
  color: white;
  font-size: 16px;
  font-weight: bold;
  background: rgba(255, 0, 0, 0.8);
  padding: 6px 12px;
  border-radius: 18px;
}

.goods-price {
  font-size: 20px;
  font-weight: bold;
  color: #ff4444;
  margin-bottom: 8px;
  display: block;

  .original-price {
    font-size: 14px;
    color: #999;
    text-decoration: line-through;
    margin-right: 8px;
  }
}

.goods-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;

  .goods-category {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
  }

  .goods-stock {
    &.low {
      color: #ff8800;
    }

    &.out {
      color: #ff4444;
    }
  }
}

.goods-description {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 10px 0;
  height: 38px; /* 两行的高度 */
}

.goods-status-tags {
  display: flex;
  gap: 4px;
  margin-top: 5px;
}

.status-tag {
  font-size: 10px;
  padding: 1px 4px;
}

.buy-btn {
  width: 100%;
  border-radius: 4px;
  height: 32px;
  font-size: 14px;

  &:disabled {
    background: #f5f5f5;
    color: #999;
    border-color: #ddd;
  }
}

.ant-card-actions {
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  padding: 8px 0;

  li {
    margin: 0;

    button {
      border-radius: 4px;
      height: 32px;
    }
  }
}

/* 淘宝风格的商品卡片边框 */
.goods-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(to right, transparent, #ff6a00, transparent);
  z-index: 2;
}

.purchase-modal-content {
  .product-summary {
    display: flex;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;

    .modal-product-image {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 4px;
      margin-right: 15px;
    }

    .product-info h3 {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: normal;
    }

    .modal-price {
      font-size: 18px;
      color: #ff4444;
      font-weight: bold;
      margin-bottom: 4px;

      .original-price {
        font-size: 14px;
        color: #999;
        text-decoration: line-through;
        margin-right: 8px;
      }
    }

    .member-discount {
      color: #1890ff;
      font-size: 12px;
      font-weight: bold;
    }

    .modal-stock {
      font-size: 12px;
      color: #999;
    }
  }

  .quantity-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }

  .total-price {
    text-align: right;
    font-size: 18px;
    font-weight: bold;
    color: #ff4444;
    padding-top: 15px;
    border-top: 1px solid #eee;
    margin-top: 15px;

    .original-total {
      font-size: 14px;
      color: #999;
      font-weight: normal;
      text-decoration: line-through;
    }
  }
}

.image-carousel {
  margin: 15px 0;

  .ant-carousel {
    .slick-slide {
      text-align: center;
      height: 200px;
      line-height: 200px;
      overflow: hidden;
    }

    img {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }
  }
}
</style>
