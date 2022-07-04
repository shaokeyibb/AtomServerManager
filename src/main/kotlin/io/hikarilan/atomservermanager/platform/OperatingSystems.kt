package io.hikarilan.atomservermanager.platform

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

object OperatingSystems {

    val operatingSystemMXBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)

}