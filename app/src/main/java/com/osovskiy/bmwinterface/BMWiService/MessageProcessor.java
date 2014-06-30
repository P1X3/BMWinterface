package com.osovskiy.bmwinterface.BMWiService;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * Created by Vadim on 6/29/2014.
 */
public class MessageProcessor
{
  private static final int MSG_MIN_SIZE = 5;
  private static final int BUFFER_SIZE = 4096;

  private byte[] _buffer;
  private int _bufferTail, _bufferHead;
  private boolean _synced;
  private EventListener _eventListener; // TODO: Add possibility to accept multiple listeners

  public void setEventListener(EventListener _eventListener)
  {
    this._eventListener = _eventListener;
  }

  public MessageProcessor()
  {
    _buffer = new byte[BUFFER_SIZE];
    _bufferTail = 0;
    _bufferHead = 0;
    _synced = false;
    _eventListener = null;
  }

  /**
   * Add data to the buffer. Called by thread that reads data from serial port.
   *
   * @param data
   */
  protected void appendBuffer(byte data[])
  {
    if (data.length > (BUFFER_SIZE - size()))
      throw new BufferOverflowException();

    for (byte aData : data)
    {
      _buffer[_bufferHead] = aData;
      _bufferHead = (_bufferHead + 1) % BUFFER_SIZE;
    }
  }

  /**
   * Peek at the first available byte in a buffer
   *
   * @return
   */
  protected byte peek()
  {
    return peek(0);
  }

  /**
   * Peek at specific byte in a buffer
   *
   * @param offset
   * @return
   */
  protected byte peek(int offset)
  {
    return _buffer[(_bufferTail + offset) % BUFFER_SIZE];
  }

  /**
   * Process data in a buffer. Process is done when there are less then 5 bytes in buffer,
   * or assumed message length is bigger than available bytes. BAD PRACTICE!
   */
  protected void process()
  {
    boolean working = true;
    while (working)
    {
      if (size() >= MSG_MIN_SIZE) // At least five bytes in buffer (minimum message length)
      {
        int assumedLength = peek(1) + 2;
        if (size() >= assumedLength) // TODO: Find an alternative since this method may cause unnecessary delays.
        {
          ByteBuffer byteBuffer = ByteBuffer.allocate(assumedLength);

          for (int i = 0; i < assumedLength; i++)
          {
            byteBuffer.put(peek(i));
          }

          BusMessage busMessage = BusMessage.tryParse(byteBuffer.array());

          if (busMessage != null)
          {
            _synced = true;
            truncate(assumedLength);

            if (_eventListener != null)
              _eventListener.newMessage(busMessage);
          }
          else
          {
            _synced = false;
            truncate();
          }
        }
        else
          working = false;
      }
      else
        working = false;
    }
  }

  /**
   * Get number of bytes in buffer
   *
   * @return
   */
  protected int size()
  {
    return (int) (BUFFER_SIZE + _bufferHead - _bufferTail) % BUFFER_SIZE;
  }

  /**
   * Truncate single byte. Used when out of sync
   */
  protected void truncate()
  {
    truncate(1);
  }

  /**
   * Truncate certain amount of bytes. Used when buffer is in sync and valid message was read
   * @param amount
   */
  protected void truncate(int amount)
  {
    _bufferTail = (_bufferTail + amount) % BUFFER_SIZE;
  }

  public interface EventListener
  {
    void newMessage(BusMessage message);
    //void onSyncStateChange(boolean state);
  }
}
