
<template>
  <div class="offline-container" style="width: 100%">
    <a-card :bordered="false" class="card-area">
      <div class="offline-grid">
        <a-card
          v-for="item in offlineList"
          :key="item.id"
          class="offline-item"
          hoverable
          :class="{ 'disabled': !canBook(item) }"
        >
          <div class="event-header">
            <div class="event-title">{{ item.title }}</div>
            <div class="event-date" :class="{ 'expired': isExpired(item.eventDate) }">
              {{ formatDate(item.eventDate) }}
            </div>
          </div>

          <div class="event-details">
            <div class="event-location">
              <a-icon type="environment" class="location-icon" />
              <span class="location-text">{{ item.location }}</span>
            </div>

            <div class="event-price-capacity">
              <div class="event-price">
                <span v-if="memberInfo" class="original-price">¥{{ item.ticketPrice.toFixed(2) }}</span>
                <span>¥{{ getDiscountedPrice(item.ticketPrice).toFixed(2) }}</span>
              </div>
              <div class="event-capacity">
                <span>容量: {{ item.totalCapacity }}</span>
                <span :class="{ 'sold-out': item.remainingCapacity <= 0 }">剩余: {{ item.remainingCapacity }}</span>
              </div>
            </div>
          </div>

          <div class="event-description">{{ item.description }}</div>

          <div class="event-actions">
            <a-button
              type="primary"
              @click="joinEvent(item)"
              :disabled="!canBook(item)"
              class="join-btn"
            >
              {{ canBook(item) ? '立即报名' : '已结束' }}
            </a-button>
            <a-button
              @click="showEventDetails(item)"
              :disabled="!canBook(item)"
              class="details-btn"
            >
              查看详情
            </a-button>
          </div>

          <!-- 遮罩层显示不可预订状态 -->
          <div
            v-if="!canBook(item)"
            class="booking-disabled-overlay"
          >
            <div class="disabled-text">{{ isExpired(item.eventDate) ? '活动已结束' : '已满员' }}</div>
          </div>
        </a-card>
      </div>
    </a-card>

    <!-- 活动详情弹窗 -->
    <a-modal
      v-model="detailModalVisible"
      :title="currentEvent ? currentEvent.title : '活动详情'"
      @cancel="hideEventDetails"
      :width="600"
      :footer="null"
    >
      <div v-if="currentEvent" class="event-detail-content">
        <div class="detail-header">
          <h2>{{ currentEvent.title }}</h2>
          <div class="detail-date">{{ formatDate(currentEvent.eventDate) }}</div>
        </div>

        <div class="detail-section">
          <h3><a-icon type="calendar" /> 活动时间</h3>
          <p>{{ formatDate(currentEvent.eventDate) }}</p>
        </div>

        <div class="detail-section">
          <h3><a-icon type="environment" /> 活动地点</h3>
          <p>{{ currentEvent.location }}</p>
          <div id="areas" style="width: 100%;height: 300px;background:#ec9e3c;color:#fff"></div>
        </div>

        <div class="detail-section">
          <h3><a-icon type="team" /> 参与人数</h3>
          <p>总容量: {{ currentEvent.totalCapacity }} | 剩余: {{ currentEvent.remainingCapacity }}</p>
        </div>

        <div class="detail-section">
          <h3><a-icon type="money" /> 门票价格</h3>
          <p>
            <span v-if="memberInfo" class="original-price">¥{{ currentEvent.ticketPrice.toFixed(2) }}</span>
            <span>¥{{ getDiscountedPrice(currentEvent.ticketPrice).toFixed(2) }}</span>
            <span v-if="memberInfo" class="member-discount">会员专享 8折优惠</span>
          </p>
        </div>

        <div class="detail-section">
          <h3><a-icon type="info-circle" /> 活动详情</h3>
          <p>{{ currentEvent.description }}</p>
        </div>

        <!--        <div class="detail-actions">-->
        <!--          <a-button-->
        <!--            type="primary"-->
        <!--            size="large"-->
        <!--            @click="joinEvent(currentEvent)"-->
        <!--            :disabled="!canBook(currentEvent)"            style="width: 100%;"-->
        <!--          >-->
        <!--            {{ canBook(currentEvent) ? '立即报名' : '活动已结束' }}-->
        <!--          </a-button>-->
        <!--        </div>-->
      </div>
    </a-modal>
  </div>
</template>

<script>import RangeDate from '@/components/datetime/RangeDate'
import BulletinAdd from './OfflineAdd.vue'
import BulletinEdit from './OfflineEdit.vue'
import {mapState} from 'vuex'
import moment from 'moment'
import baiduMap from '@/utils/map/baiduMap'
moment.locale('zh-cn')

export default {
  name: 'Offline',
  components: {BulletinAdd, BulletinEdit, RangeDate},
  data () {
    return {
      offlineList: [],
      detailModalVisible: false,
      currentEvent: null,
      memberInfo: null,
      nowPoint: null
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  mounted () {
    this.queryOfflineList()
    this.selectMemberByUserId()
  },
  methods: {
    selectMemberByUserId () {
      this.$get(`/cos/member-order-info/member/${this.currentUser.userId}`).then((r) => {
        this.memberInfo = r.data.member
      })
    },
    queryOfflineList () {
      this.$get('/cos/offline-events/list').then((r) => {
        this.offlineList = r.data.data
      })
    },
    // 获取折扣后的价格
    getDiscountedPrice(price) {
      if (this.memberInfo) {
        return price * 0.8 // 8折优惠
      }
      return price
    },
    formatDate(dateString) {
      return moment(dateString).format('YYYY年MM月DD日')
    },
    // 判断活动是否已过期
    isExpired(eventDate) {
      const eventMoment = moment(eventDate)
      const now = moment()
      return eventMoment.isBefore(now, 'day') // 比较日期，不考虑时间
    },
    // 判断活动是否可以预订
    canBook(item) {
      return item.remainingCapacity > 0 && !this.isExpired(item.eventDate)
    },
    joinEvent(item) {
      if (this.canBook(item)) {
        console.log('报名活动:', item)
        // 这里可以跳转到报名页面或执行报名逻辑
        let discountedPrice = this.getDiscountedPrice(item.ticketPrice)
        let data = {orderPrice: discountedPrice, eventId: item.id, userId: this.currentUser.userId}
        this.$post('/cos/pay/offline', data).then((r) => {
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
      } else {
        if (this.isExpired(item.eventDate)) {
          this.$message.warning('该活动已结束')
        } else {
          this.$message.warning('该活动已满员')
        }
      }
    },
    getLocal () {
      // eslint-disable-next-line no-undef
      let geolocation = new BMap.Geolocation()
      geolocation.getCurrentPosition(r => {
        this.nowPoint = r.point
      }, {enableHighAccuracy: true})
    },
    local (scenic) {
      baiduMap.clearOverlays()
      baiduMap.rMap().enableScrollWheelZoom(true)
      // eslint-disable-next-line no-undef
      let point = new BMap.Point(scenic.longitude, scenic.latitude)
      baiduMap.pointAdd(point)
      baiduMap.findPoint(point, 16)
      // let driving = new BMap.DrivingRoute(baiduMap.rMap(), {renderOptions:{map: baiduMap.rMap(), autoViewport: true}});
      // driving.search(new BMap.Point(this.nowPoint.lng,this.nowPoint.lat), new BMap.Point(scenic.point.split(",")[0],scenic.point.split(",")[1]));
    },
    // 显示活动详情弹窗
    showEventDetails(item) {
      if (this.canBook(item)) {
        this.currentEvent = item
        this.detailModalVisible = true
        setTimeout(() => {
          baiduMap.initMap('areas')
          this.getLocal()
          setTimeout(() => {
            this.local(item)
          }, 500)
        }, 200)
      } else {
        if (this.isExpired(item.eventDate)) {
          this.$message.info('该活动已结束，无法查看详情')
        } else {
          this.$message.info('该活动已满员，无法查看详情')
        }
      }
    },
    // 隐藏活动详情弹窗
    hideEventDetails() {
      this.detailModalVisible = false
      this.currentEvent = null
    }
  },
  watch: {}
}
</script>

<style lang="less" scoped>@import "../../../../static/less/Common";

.offline-container {
  padding: 20px;
}

.card-area {
  background: #fff;
}

.offline-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  padding: 10px;
}

.offline-item {
  position: relative;
  margin-bottom: 0;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: visible;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover:not(.disabled) {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0,0,0,0.1);
  }

  &.disabled {
    opacity: 0.7;
    filter: grayscale(30%);
  }
}

.booking-disabled-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  z-index: 10;
}

.disabled-text {
  color: white;
  font-size: 18px;
  font-weight: bold;
  background: rgba(255, 0, 0, 0.8);
  padding: 8px 16px;
  border-radius: 20px;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .event-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }

  .event-date {
    font-size: 14px;
    color: #1890ff;
    background: #e6f7ff;
    padding: 4px 10px;
    border-radius: 12px;

    &.expired {
      color: #ff4d4f;
      background: #fff2f0;
    }
  }
}

.event-details {
  padding: 16px 20px;

  .event-location {
    display: flex;
    align-items: center;
    margin-bottom: 12px;

    .location-icon {
      color: #52c41a;
      margin-right: 8px;
      font-size: 16px;
    }

    .location-text {
      color: #666;
      font-size: 14px;
    }
  }

  .event-price-capacity {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .event-price {
      font-size: 20px;
      font-weight: bold;
      color: #ff4444;
      display: flex;
      align-items: center;

      .original-price {
        font-size: 14px;
        color: #999;
        text-decoration: line-through;
        margin-right: 8px;
      }
    }

    .event-capacity {
      display: flex;
      gap: 15px;
      font-size: 13px;
      color: #666;

      .sold-out {
        color: #ff4d4f;
        font-weight: bold;
      }
    }
  }
}

.event-description {
  padding: 0 20px 16px;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.event-actions {
  padding: 16px 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;

  .join-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 4px;

    &:disabled {
      background: #f5f5f5;
      color: #aaa;
      border-color: #ddd;
    }
  }

  .details-btn {
    border-radius: 4px;

    &:disabled {
      background: #f5f5f5;
      color: #aaa;
      border-color: #ddd;
    }
  }
}

// 活动详情弹窗样式
.event-detail-content {
  .detail-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;

    h2 {
      margin: 0 0 10px 0;
      font-size: 22px;
      color: #333;
    }

    .detail-date {
      font-size: 16px;
      color: #1890ff;
      background: #e6f7ff;
      padding: 4px 10px;
      border-radius: 4px;
      display: inline-block;
    }
  }

  .detail-section {
    margin-bottom: 20px;

    h3 {
      margin: 0 0 8px 0;
      font-size: 16px;
      color: #333;
      display: flex;
      align-items: center;

      .anticon {
        margin-right: 8px;
        color: #1890ff;
      }
    }

    p {
      margin: 0;
      color: #666;
      line-height: 1.6;
      display: flex;
      align-items: center;
      gap: 8px;

      .original-price {
        font-size: 14px;
        color: #999;
        text-decoration: line-through;
      }

      .member-discount {
        font-size: 12px;
        color: #1890ff;
        background: #e6f7ff;
        padding: 2px 6px;
        border-radius: 4px;
      }
    }
  }

  .detail-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}
</style>
