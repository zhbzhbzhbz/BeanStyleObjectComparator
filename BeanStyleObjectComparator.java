import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

import com.alibaba.fastjson.JSON;

/*
 * JavaBeanStyleObject�Ƚ���
 * 1������1��ͨ��fastjson������ת����json�ַ������Ƚ�
 * 2������2��ͨ���ݹ������ж�ÿ�������ı�������ֵ��
 */
public class BeanStyleObjectComparator {
	// �Լ�ʵ����equals�ĳ�����������
		static Class<?> SupportedClasses[] = { Integer.class, String.class, Boolean.class, Character.class, Byte.class,
				Short.class, Long.class, Float.class, Double.class, ArrayList.class, LinkedList.class, Date.class,
				HashSet.class, TreeSet.class, LinkedHashSet.class };
		static ArrayList<Class<?>> SupportedClassesList = null;

		static {
			SupportedClassesList = new ArrayList<Class<?>>();
			Collections.addAll(SupportedClassesList, SupportedClasses);// ת��ΪList
		}
		//ʹ��fastjson��
		static public boolean compareByJson(Object a, Object b) {
			String jsona = JSON.toJSONString(a);
			String jsonb = JSON.toJSONString(b);
			return jsona.equals(jsonb);
		}

		// ���¡����Ϊcompare���޸Ķ���ı�����Ȩ�ޣ�
		static public Object deepClone(Object o) {
			// ������д������
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = null;
			try {
				oo = new ObjectOutputStream(bo);
				oo.writeObject(o);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// �����������
			ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
			ObjectInputStream oi = null;
			try {
				oi = new ObjectInputStream(bi);
				return (oi.readObject());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		static public boolean compare(Object obj_a, Object obj_b) {
			if (obj_a == null && obj_b == null) {
				return true;
			} else if (obj_a == null || obj_b == null) {
				return false;
			} else {
				Field[] fields_a = obj_a.getClass().getDeclaredFields();
				Field[] fields_b = obj_b.getClass().getDeclaredFields();
				// �������������
				if (fields_a.length != fields_b.length)
					return false;
				// else if(fields_a.length==0 && fields_b.length) �����ԣ�������һ������Class
				// A{}Ҳ��һ�����صĳ�Ա����this$0
				else
					for (int i = 0; i < fields_a.length; i++) {
						// ��ֹ�е���private��
						fields_a[i].setAccessible(true);
						fields_b[i].setAccessible(true);

						// �Ȼ�����������ʵ��
						Object obj_a_innerobj_i = null, obj_b_innerobj_i = null;
						try {
							obj_a_innerobj_i = fields_a[i].get(obj_a);
							obj_b_innerobj_i = fields_b[i].get(obj_b);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						System.out.println("������:" + fields_a[i].getName() + ";" + fields_b[i].getName());
						System.out.println("��������:" + fields_a[i].getGenericType() + ";" + fields_b[i].getGenericType());
						// �����������ͬ,��false
						if (fields_a[i].getName() != fields_b[i].getName()) {
							return false;
						}
						// ����������Ͳ�ͬ����false(ע���˴�������fields_a[i].getClass().toString()!!���򶼻᷵��Field��)
						else if (!fields_a[i].getGenericType().equals(fields_b[i].getGenericType())) {
							return false;
						}
						// �����ԣ�����Integer��int��getclass()��ͬ.new
						// ArrayList().getClass()��ArrayList.classҲ��ͬ��
						// ����List��ArrayList��ͬ���������Ժ���������List����ʽ����������Ҳ����ArrayList.
						// ����������ǳ�������,�ͼ�������Ƚ�.
						else if (!SupportedClassesList.contains(obj_a_innerobj_i.getClass())) {
							if (compare(obj_a_innerobj_i, obj_b_innerobj_i) == false)
								return false;
							// ��������Ǽ��ϣ���ֵ�����()
						} else if (obj_a_innerobj_i instanceof Collection) {
							// TODO �����Ƶĵط�
							// if(!CollectionUtils.isEqualCollection((Collection<?>)obj_a_innerobj_i,
							// (Collection<?>)obj_b_innerobj_i))
							if (!(((Collection) obj_a_innerobj_i).containsAll((Collection) (obj_b_innerobj_i))
									&& ((Collection) obj_b_innerobj_i).containsAll((Collection) (obj_a_innerobj_i))))
								return false;
						}
						// ������߲��Ǽ��ϣ���ֵ�����
						else if (!obj_a_innerobj_i.equals(obj_b_innerobj_i)) {
							return false;
						}
					}
			}
			return true;
		}
}
