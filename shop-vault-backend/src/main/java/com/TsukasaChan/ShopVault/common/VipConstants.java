package com.TsukasaChan.ShopVault.common;

import java.math.BigDecimal;

public final class VipConstants {

    private VipConstants() {
    }

    public static final BigDecimal VIP_MONTHLY_PRICE = new BigDecimal("99.00");
    public static final BigDecimal VIP_YEARLY_PRICE = new BigDecimal("999.00");
    public static final BigDecimal SVIP_YEARLY_PRICE = new BigDecimal("1499.00");

    public static final int VIP_MONTHLY_POINTS = 3000;
    public static final int VIP_YEARLY_POINTS = 25000;
    public static final int SVIP_YEARLY_POINTS = 20000;

    public static final int TYPE_VIP_MONTHLY = 1;
    public static final int TYPE_VIP_YEARLY = 2;
    public static final int TYPE_SVIP_YEARLY = 3;

    public static final int LEVEL_NORMAL = 0;
    public static final int LEVEL_VIP = 1;
    public static final int LEVEL_SVIP = 2;

    public static final int DURATION_VIP_MONTHLY = 30;
    public static final int DURATION_VIP_YEARLY = 365;
    public static final int DURATION_SVIP_YEARLY = 365;

    public static BigDecimal getPriceByType(int vipType) {
        return switch (vipType) {
            case TYPE_VIP_MONTHLY -> VIP_MONTHLY_PRICE;
            case TYPE_VIP_YEARLY -> VIP_YEARLY_PRICE;
            case TYPE_SVIP_YEARLY -> SVIP_YEARLY_PRICE;
            default -> BigDecimal.ZERO;
        };
    }

    public static int getPointsByType(int vipType) {
        return switch (vipType) {
            case TYPE_VIP_MONTHLY -> VIP_MONTHLY_POINTS;
            case TYPE_VIP_YEARLY -> VIP_YEARLY_POINTS;
            case TYPE_SVIP_YEARLY -> SVIP_YEARLY_POINTS;
            default -> 0;
        };
    }

    public static int getDurationByType(int vipType) {
        return switch (vipType) {
            case TYPE_VIP_MONTHLY -> DURATION_VIP_MONTHLY;
            case TYPE_VIP_YEARLY, TYPE_SVIP_YEARLY -> DURATION_VIP_YEARLY;
            default -> 0;
        };
    }

    public static int getLevelByType(int vipType) {
        return switch (vipType) {
            case TYPE_VIP_MONTHLY, TYPE_VIP_YEARLY -> LEVEL_VIP;
            case TYPE_SVIP_YEARLY -> LEVEL_SVIP;
            default -> LEVEL_NORMAL;
        };
    }
}
