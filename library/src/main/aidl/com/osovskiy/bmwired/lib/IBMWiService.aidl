// IBMWiService.aidl
package com.osovskiy.bmwired.lib;

import com.osovskiy.bmwired.lib.BusMessage;
import com.osovskiy.bmwired.lib.IBMWiServiceCallback;

interface IBMWiService
{
  void sendMessageToBus(in BusMessage msg);
  void sendMessageFromBus(in BusMessage msg);

  String registerCallback(IBMWiServiceCallback callback);
  void unregisterCallback(String uuid);
}
