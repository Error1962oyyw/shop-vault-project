package com.TsukasaChan.ShopVault.common;

public final class EntityConstants {

    private EntityConstants() {
    }

    public static final class Status {
        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
        
        private Status() {}
    }

    public static final class OrderStatus {
        public static final int UNPAID = 0;
        public static final int PAID = 1;
        public static final int SHIPPED = 2;
        public static final int RECEIVED = 3;
        public static final int CANCELLED = 4;
        public static final int REFUNDING = 5;
        public static final int REFUNDED = 6;
        
        private OrderStatus() {}
    }

    public static final class CouponStatus {
        public static final int UNUSED = 0;
        public static final int USED = 1;
        public static final int EXPIRED = 2;
        
        private CouponStatus() {}
    }

    public static final class Gender {
        public static final int UNKNOWN = 0;
        public static final int MALE = 1;
        public static final int FEMALE = 2;
        
        private Gender() {}
    }

    public static final class BooleanFlag {
        public static final int NO = 0;
        public static final int YES = 1;
        
        private BooleanFlag() {}
    }

    public static boolean isEnabled(Integer status) {
        return status != null && status == Status.ENABLED;
    }

    public static boolean isDisabled(Integer status) {
        return status == null || status == Status.DISABLED;
    }

    public static boolean isYes(Integer flag) {
        return flag != null && flag == BooleanFlag.YES;
    }

    public static boolean isNo(Integer flag) {
        return flag == null || flag == BooleanFlag.NO;
    }
}
