#ifndef DEProperty_H
#define DEProperty_H

//#include "DENetwork.h"
#include "utility"

////////////////////////////////////////////////////////////////////////////////////////////////////
/// <summary>	Basic packet that defines the information to be sent and received from the server. </summary>
///
/// <remarks>	Sunny, 5/28/2010. </remarks>
////////////////////////////////////////////////////////////////////////////////////////////////////

class BasicPacket
{
public:
	BasicPacket() {}
	virtual ~BasicPacket() {}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the data as a void pointer. </summary>
	///
	/// <returns>	null if it fails, else the data as void pointer. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual void* getDataAsVoidPtr() = 0;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the size of the data enclosed in the packet. </summary>
	///
	/// <returns>	The data size. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual long getDataSize() = 0;

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the command number.</summary>
	///
	/// <returns>	The command. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	int getCommand() { return this->cmdType; };

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Query if this object is read only. </summary>
	///
	/// <returns>	true if read only, false if not. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	bool isReadOnly() { return this->readOnly; }
protected:
	int cmdType;
	bool readOnly;
};

////////////////////////////////////////////////////////////////////////////////////////////////////
/// <summary>	Buffer Packet to get from the server a set of data of arbitrary size.  Used primarily
///             to represent images. In contrast to PropertyPacket, the buffer to which the data is written
///             to is allocated externally and passed in via a pointer.</summary>
///
/// <remarks>	Sunny, 5/28/2010. </remarks>
////////////////////////////////////////////////////////////////////////////////////////////////////

class BufferPacket : public BasicPacket
{
public:

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Constructor</summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <param name="cmdVal">	The command value. </param>
	/// <param name="buffer">	[in] The buffer to be written to. </param>
	/// <param name="size">		The sizeof the buffer passed in. </param>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	BufferPacket(int cmdVal, void* buffer, long size)
	{
		this->data = buffer;
		this->dataSize = size;
		this->readOnly = true;
		this->cmdType = cmdVal;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the buffer. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <returns>	null if it fails, else the data as void pointer. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual void* getDataAsVoidPtr() { return this->data; }

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the data size. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <returns>	The data size. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual long getDataSize() { return dataSize; }

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Sets a new buffer. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <param name="buffer">	[in] If non-null, the buffer. </param>
	/// <param name="size">		The size. </param>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	void setNewBuffer(void* buffer, long size) { 
		data = buffer;
		dataSize = size;
	}
private:
	void* data;
	long dataSize;
};

////////////////////////////////////////////////////////////////////////////////////////////////////
/// <summary>	Property packet used to represent a fixed set of values which are represented as a 
///             type. The type (either a primitive or a struct) can then be used to figure out the size
///             of the packet in addition to allocating an internal buffer of that type.</summary>
///
/// <remarks>	Sunny, 5/28/2010. </remarks>
////////////////////////////////////////////////////////////////////////////////////////////////////

template<typename T>
class PropertyPacket : public BasicPacket
{
public:

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Constructor. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <param name="cmdVal">		The command value. </param>
	/// <param name="readOnlyVal">	Set to true to be read only, false otherwise. </param>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	PropertyPacket(int cmdVal, bool readOnlyVal)
	{
		this->readOnly = readOnlyVal;
		this->cmdType = cmdVal;
	}
	virtual ~PropertyPacket() {}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the data as void pointer. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <returns>	null if it fails, else the data as void pointer. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual void* getDataAsVoidPtr() { return &this->data; }

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary>	Gets the data size. </summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <returns>	The data size. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	virtual long getDataSize() { return sizeof(T); }

	////////////////////////////////////////////////////////////////////////////////////////////////////
	/// <summary> Returns the pointer to the internally held buffer.</summary>
	///
	/// <remarks>	Sunny, 5/28/2010. </remarks>
	///
	/// <returns>	null if it fails, else the data. </returns>
	////////////////////////////////////////////////////////////////////////////////////////////////////

	T* getData() { return &this->data; }
private:
	T data;
};

#endif