/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainDescriberMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

@Singleton
public class MessageParserService implements MessageHandler {

  private Logger _log = LoggerFactory.getLogger(MessageParserService.class);

  private Gson _gson;

  private TrainMovementHandler _trainMovementHandler;

  private TrainDescriberHandler _trainDescriberHandler;

  @Inject
  public void setGson(Gson gson) {
    _gson = gson;
  }

  @Inject
  public void setTrainMovementHandler(TrainMovementHandler trainMovementHandler) {
    _trainMovementHandler = trainMovementHandler;
  }

  @Inject
  public void setTrainDescriberHandler(
      TrainDescriberHandler trainDescriberHandler) {
    _trainDescriberHandler = trainDescriberHandler;
  }

  @Override
  public void processMessage(long fileTimestamp, EMessageType messageType,
      String jsonMessage, String source) throws IOException {
    if (!jsonMessage.startsWith("[")) {
      return;
    }
    switch (messageType) {
      case TRAIN_MOVEMENT: {
        TrainMovementMessage[] messages = _gson.fromJson(jsonMessage,
            TrainMovementMessage[].class);
        for (TrainMovementMessage message : messages) {
          _trainMovementHandler.handleTrainMovementMessage(fileTimestamp,
              message, source);
        }
        break;
      }
      case TD: {
        TrainDescriberMessage[] messages = _gson.fromJson(jsonMessage,
            TrainDescriberMessage[].class);
        for (TrainDescriberMessage message : messages) {
          _trainDescriberHandler.handleTrainDescriberMessage(fileTimestamp,
              message, source);
        }
      }
    }
  }
}
