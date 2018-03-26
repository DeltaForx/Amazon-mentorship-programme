package com.example;
import java.util.*;
class messageBody
{
public String messageText;
public int redeliveryCount;
public messageBody(String str, int count)
{
this.messageText = str;
this.redeliveryCount = count;
}
public String getText()
{
return messageText;
}
public int getCount()
{
return redeliveryCount;
}
}