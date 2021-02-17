package com.payu.kube.log.stage

import javafx.stage.Stage
import org.springframework.context.ApplicationEvent

class StageReadyEvent(val stage: Stage) : ApplicationEvent(stage)
