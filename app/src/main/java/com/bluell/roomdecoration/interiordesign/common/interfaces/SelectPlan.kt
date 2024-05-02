package com.bluell.roomdecoration.interiordesign.common.interfaces

import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOPlans

interface SelectPlan {

    fun selectedPlan(dtoPlans: DTOPlans)
}