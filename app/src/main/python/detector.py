from ultralytics import YOLO
import cv2
import os

def detect(image):
    '''박스 감지 후, crop한 이미지와 crop 범위 리턴'''
    module_path = os.path.dirname(os.path.realpath(__file__))
    model_path = os.path.join(module_path, 'detect_model.pt')
    model = YOLO(model_path)
    source = image
    model.predict(source, imgsz=640, conf=0.5, max_det=1)
    results = model(source)
    boxes = results[0].boxes
    box = boxes[0]  # returns one box
    
    res = results[0].plot(boxes=False)
    lt = box.xyxy[0][:2].tolist() # lefttop
    rb = box.xyxy[0][2:].tolist() # rightbottom

    res_crop = res[int(lt[1]):int(rb[1]), int(lt[0]):int(rb[0])]

    return res_crop, box.xyxy
    #cv2.imshow("detect, crop",res_crop)
    #cv2.waitKey()