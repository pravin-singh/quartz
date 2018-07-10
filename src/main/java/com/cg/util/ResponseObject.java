package com.cg.util;

import java.util.Set;

public class ResponseObject { 
		private boolean success = false;
		private Set<BatchDetail> data;
			
		public ResponseObject(boolean success, Set<BatchDetail> data) {
			super();
			this.success = success;
			this.data = data;
		}
		public ResponseObject() {
			super();
			// TODO Auto-generated constructor stub
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public Set<BatchDetail> getData() {
			return data;
		}
		public void setData(Set<BatchDetail> data) {
			this.data = data;
		}
   

   
}
